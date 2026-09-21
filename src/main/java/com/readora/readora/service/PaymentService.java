package com.readora.readora.service;

import com.readora.readora.dto.PaymentInitResponse;
import com.readora.readora.model.*;
import com.readora.readora.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private static final Map<String, Double> PLAN_PRICING = Map.of(
            "FREE", 0.0,
            "PREMIUM", 450.0,
            "INSTITUTIONAL", 2400.0
    );

    private final PaymentRepository paymentRepository;
    private final TransactionRepository transactionRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PurchaseRepository purchaseRepository;
    private final BookRepository bookRepository;
    private final SubscriptionService subscriptionService;
    private final NotificationService notificationService;
    private final EsewaPaymentService esewaPaymentService;

    public PaymentService(PaymentRepository paymentRepository,
                          TransactionRepository transactionRepository,
                          SubscriptionRepository subscriptionRepository,
                          PurchaseRepository purchaseRepository,
                          BookRepository bookRepository,
                          SubscriptionService subscriptionService,
                          NotificationService notificationService,
                          EsewaPaymentService esewaPaymentService) {
        this.paymentRepository = paymentRepository;
        this.transactionRepository = transactionRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.purchaseRepository = purchaseRepository;
        this.bookRepository = bookRepository;
        this.subscriptionService = subscriptionService;
        this.notificationService = notificationService;
        this.esewaPaymentService = esewaPaymentService;
    }

    // =========================================================
    // INITIATE
    // =========================================================

    public PaymentInitResponse initiate(User user, String paymentType, String plan, Long bookId, List<Long> bookIds) {
        double amount = resolveAmount(paymentType, plan, bookId, bookIds);
        String transactionUuid = UUID.randomUUID().toString();

        Payment payment = new Payment(user, paymentType, plan, bookId, amount, "ESEWA", transactionUuid);

        if ("CART_PURCHASE".equals(paymentType) && bookIds != null) {
            payment.setCartBookIds(bookIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
        }

        payment = paymentRepository.save(payment);

        if (amount <= 0) {
            // FREE plan or an already-owned/empty cart — nothing to charge, fulfill immediately
            fulfill(payment);
            payment.setStatus("SUCCESS");
            payment.setCompletedAt(Instant.now());
            paymentRepository.save(payment);
            return PaymentInitResponse.alreadyFree(payment.getId());
        }

        Map<String, String> fields = esewaPaymentService.buildFormFields(transactionUuid, amount);
        return PaymentInitResponse.gatewayForm(payment.getId(), esewaPaymentService.getFormActionUrl(), fields);
    }

    // =========================================================
    // CALLBACK
    // =========================================================

    public void handleSuccess(String base64Data) throws Exception {
        Map<String, Object> data = esewaPaymentService.decodeCallback(base64Data);

        if (!esewaPaymentService.verifySignature(data)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "eSewa signature verification failed.");
        }

        String transactionUuid = String.valueOf(data.get("transaction_uuid"));
        Payment payment = paymentRepository.findByGatewayReference(transactionUuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found."));

        if ("SUCCESS".equals(payment.getStatus())) return; // already processed, don't double-fulfill

        double totalAmount = Double.parseDouble(String.valueOf(data.get("total_amount")).replace(",", ""));
        Map<String, Object> statusCheck = esewaPaymentService.checkStatus(transactionUuid, totalAmount);
        String status = String.valueOf(statusCheck.get("status"));

        transactionRepository.save(new Transaction(
                payment, "ESEWA",
                String.valueOf(statusCheck.get("ref_id")),
                statusCheck.toString(),
                status
        ));

        if ("COMPLETE".equalsIgnoreCase(status)) {
            payment.setStatus("SUCCESS");
            payment.setCompletedAt(Instant.now());
            paymentRepository.save(payment);
            fulfill(payment);
        } else {
            payment.setStatus("FAILED");
            paymentRepository.save(payment);
        }
    }

    public void handleFailure(String transactionUuid) {
        if (transactionUuid == null) return;
        paymentRepository.findByGatewayReference(transactionUuid).ifPresent(payment -> {
            payment.setStatus("FAILED");
            paymentRepository.save(payment);
        });
    }

    // =========================================================
    // FULFILLMENT
    // =========================================================

    private void fulfill(Payment payment) {
        User user = payment.getUser();

        if ("SUBSCRIPTION".equals(payment.getPaymentType())) {
            Subscription subscription = subscriptionService.createSubscription(user.getId(), payment.getPlan());
            subscription.setPaymentMethod("eSewa");
            subscription.setAmount(payment.getAmount());
            subscriptionRepository.save(subscription);

            notificationService.createNotification(
                    user, "Subscription activated",
                    "Your " + payment.getPlan() + " subscription is now active.",
                    "SUBSCRIPTION", "/subscription"
            );

        } else if ("BOOK_PURCHASE".equals(payment.getPaymentType())) {
            Book book = bookRepository.findById(payment.getBookId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found."));

            if (!purchaseRepository.existsByUserAndBookId(user, book.getId())) {
                purchaseRepository.save(new Purchase(user, book));
            }

            notificationService.createNotification(
                    user, "Purchase complete",
                    "You now have full access to \"" + book.getTitle() + "\".",
                    "PURCHASE", "/books/" + book.getId()
            );

        } else if ("CART_PURCHASE".equals(payment.getPaymentType())) {
            List<Long> ids = parseCartBookIds(payment.getCartBookIds());
            List<String> unlockedTitles = new ArrayList<>();

            for (Long id : ids) {
                Book book = bookRepository.findById(id).orElse(null);
                if (book == null) continue;

                if (!purchaseRepository.existsByUserAndBookId(user, book.getId())) {
                    purchaseRepository.save(new Purchase(user, book));
                }
                unlockedTitles.add(book.getTitle());
            }

            notificationService.createNotification(
                    user, "Purchase complete",
                    unlockedTitles.isEmpty()
                            ? "Your cart purchase is complete."
                            : "You now have full access to " + unlockedTitles.size() + " book(s).",
                    "PURCHASE", "/library"
            );
        }
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private double resolveAmount(String paymentType, String plan, Long bookId, List<Long> bookIds) {
        if ("SUBSCRIPTION".equals(paymentType)) {
            if (plan == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Plan is required.");
            Double price = PLAN_PRICING.get(plan.toUpperCase());
            if (price == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid plan.");
            return price;

        } else if ("BOOK_PURCHASE".equals(paymentType)) {
            if (bookId == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "bookId is required.");
            Book book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found."));
            return book.getPrice() != null ? book.getPrice() : 0.0;

        } else if ("CART_PURCHASE".equals(paymentType)) {
            if (bookIds == null || bookIds.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "bookIds must contain at least one book.");
            }
            double total = 0.0;
            for (Long id : bookIds) {
                Book book = bookRepository.findById(id)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found: " + id));
                total += book.getPrice() != null ? book.getPrice() : 0.0;
            }
            return total;
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "paymentType must be SUBSCRIPTION, BOOK_PURCHASE, or CART_PURCHASE.");
    }

    private List<Long> parseCartBookIds(String csv) {
        if (csv == null || csv.isBlank()) return List.of();
        return java.util.Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .toList();
    }

    public List<Payment> getUserPayments(User user) {
        return paymentRepository.findByUserOrderByCreatedAtDesc(user);
    }
}
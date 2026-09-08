package com.readora.readora.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

  @GetMapping
  public Map<String, Object> getDashboard() {

    Map<String, Object> user = Map.of(
            "name", "Laxmi Sapkota"
    );

    Map<String, Object> session = Map.of(
            "minutes", 38,
            "streak", 42,
            "inProgress", 3
    );

    Map<String, Object> currentBook = Map.of(
            "title", "The Mountain Path",
            "author", "Tenzing Norbu",
            "category", "NEPALI HIMALAYAN LITERATURE",
            "progress", 68,
            "cover", "img/TheSilentSpring.png",
            "notes", 14
    );

    List<Map<String, Object>> recommendations = List.of(

            Map.of(
                    "title", "Seto Dharti (सेतो धरती)",
                    "author", "Amar Neupane",
                    "category", "NEPALI FICTION",
                    "match", "98% AI Match",
                    "description",
                    "A profound exploration of cultural resilience and human spirit.",
                    "theme", "light",
                    "price", "NPR 499",
                    "action", "Unlock Book"
            ),

            Map.of(
                    "title", "The Architecture of Silence",
                    "author", "Alistair Sterling",
                    "category", "ARCHITECTURE",
                    "match", "94% AI Match",
                    "description",
                    "Examining monastery acoustics and solitary retreats throughout High Asia.",
                    "theme", "dark",
                    "price", "Included in Sub",
                    "action", "Start Reading"
            ),

            Map.of(
                    "title", "Shirishko Phool • शिरीषको फूल",
                    "author", "Parijat",
                    "category", "DEVANAGARI CLASSICS",
                    "match", "91% AI Match",
                    "description",
                    "Madan Puraskar winning modernist novel capturing existential reflection.",
                    "theme", "beige",
                    "price", "Public Domain",
                    "action", "Read Archive"
            )
    );

    List<List<String>> papers = List.of(

            List.of(
                    "THE HIMALAYAN REVIEW",
                    "Vol. 42 • Morning Edition",
                    "Climate Resilience & High-Altitude Agricultural Innovations in Manang",
                    "Special field report analyzing water harvest systems and localized terrace farming.",
                    "8 min read • 9 Selected Highlights"
            ),

            List.of(
                    "KATHMANDU LITERARY CHRONICLE",
                    "Weekly Digest",
                    "Preserving Lost Newari Ballads: A Century of Patan's Oral History",
                    "Archivists translate and annotate songbooks recorded during the Malla period.",
                    "12 min read • Archival Audio Attached"
            )
    );

    List<List<String>> notes = List.of(

            List.of(
                    "The Mountain Path • Ch. 4",
                    "Yesterday",
                    "The cacophony of the city we had left behind felt like a distant dream.",
                    "Personal reflection added"
            ),

            List.of(
                    "Meditations • Book IV",
                    "3 days ago",
                    "The soul becomes dyed with the color of its thoughts.",
                    "Tagged: #stoicism #clarity"
            )
    );

    return Map.of(
            "user", user,
            "session", session,
            "currentBook", currentBook,
            "recommendations", recommendations,
            "papers", papers,
            "notes", notes
    );
  }
}
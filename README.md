# MoodyDay
AMIT3353 Mobile Application Development - Assignment
<br>
This repository contains an academic assignment. It is made public for portfolio and demonstration purposes only.


# About The Project
A weather tracking Android app built for a university mobile development course, focused on UN SDG 13 (Climate Action). Standard weather forecasting, the app helps users understand climate trends, set weather-aware personal goals, and receive actionable, condition-based tips — combining real-time API data with a fully local + cloud-synced data layer.

# Tech Stack
Language: Kotlin<br>
Framework/UI: Jetpack Compose<br>
Database: Room(SQLite | Local), Supabase(Remote)<br>
Networking: Retrofit<br>
APIs: Geocoding, Weather(Open-Meteo), Nominatim(OpenStreetMap)<br>

# Key Features
Live weather dashboard — real-time conditions, "feels like" temperature, humidity, wind, UV index, with condition-based dynamic iconography

City search & management — geocoded city search, saved/favorite cities, and GPS-based "current location" detection with reverse geocoding

Climate insight engine — compares live and forecasted temperatures against a computed historical average (10-year window), surfacing dynamic warmer/cooler/neutral insights rather than static text

7-day forecast with historical comparison chart — visual bar chart highlighting the day with the most significant deviation from climate norms

Custom weather alerts — user-defined threshold rules (e.g. temperature, rainfall) evaluated live against real weather data

Weather-linked climate action tips — condition-based suggestions (e.g. energy-saving, outfit recommendations) generated from live data

Gamified climate goals — habit-tracking goals with streaks, designed to connect user actions to real weather conditions (e.g. suggesting good days for outdoor activities based on forecast)

Full user authentication — Supabase Auth-backed login/registration, with per-user data isolation enforced

Offline-first + cloud sync architecture — every core data type (saved cities, notes, goals, alert rules, settings) is stored locally in Room and synced to Supabase, satisfying both offline reliability and cross-device persistence 

# Team
Built by a 4-person team, each owning a full feature module

1. Core Weather & Cities
- Live dashboard, city search/geocoding, saved cities; also set up the shared project infrastructure (navigation system, database scaffolding, Supabase/API integration, authentication migration)

2. Forecast & Climate Notes
- 7-day forecast, personal weather-observation notes, and historical climate data integration (Open-Meteo Archive API) powering the temp-vs-historical-average comparison

3. Alerts & Climate Action
- Custom weather alert rules, condition-based climate tips, and UI/visual design for the module; severity-coded alert cards, styled rule management, and outfit recommendation cards

4. Goals & Settings
- Gamified climate goals with streaks, user profile and app preferences, plus Supabase Authentication integration (registration/login)

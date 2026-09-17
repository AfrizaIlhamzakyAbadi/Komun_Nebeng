import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

class AppTheme {
  static const Color matteBlack = Color(0xFF121212);
  static const Color charcoal = Color(0xFF1E1E1E);
  static const Color crimsonRed = Color(0xFFDC143C);
  static const Color textWhite = Color(0xFFFFFFFF);
  static const Color textGrey = Color(0xFFAAAAAA);

  static ThemeData get darkTheme {
    return ThemeData(
      brightness: Brightness.dark,
      scaffoldBackgroundColor: matteBlack,
      primaryColor: crimsonRed,
      colorScheme: const ColorScheme.dark(
        primary: crimsonRed,
        secondary: crimsonRed,
        surface: charcoal,
        onSurface: textWhite,
        onPrimary: textWhite,
      ),
      appBarTheme: const AppBarTheme(
        backgroundColor: matteBlack,
        elevation: 0,
        centerTitle: true,
        iconTheme: IconThemeData(color: textWhite),
      ),
      cardTheme: CardThemeData(
        color: charcoal,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(12),
        ),
        elevation: 4,
      ),
      elevatedButtonTheme: ElevatedButtonThemeData(
        style: ElevatedButton.styleFrom(
          backgroundColor: crimsonRed,
          foregroundColor: textWhite,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(8),
          ),
          padding: const EdgeInsets.symmetric(vertical: 16, horizontal: 24),
          textStyle: GoogleFonts.inter(fontWeight: FontWeight.bold),
        ),
      ),
      textTheme: GoogleFonts.interTextTheme(ThemeData.dark().textTheme).copyWith(
        bodyLarge: GoogleFonts.inter(color: textWhite),
        bodyMedium: GoogleFonts.inter(color: textGrey),
        titleLarge: GoogleFonts.inter(color: textWhite, fontWeight: FontWeight.bold),
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: charcoal,
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(8),
          borderSide: BorderSide.none,
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(8),
          borderSide: const BorderSide(color: crimsonRed, width: 2),
        ),
        labelStyle: const TextStyle(color: textGrey),
      ),
    );
  }
}

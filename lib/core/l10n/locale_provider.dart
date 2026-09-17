import 'package:flutter_riverpod/flutter_riverpod.dart';

class LocaleNotifier extends Notifier<String> {
  @override
  String build() => 'en';

  void setLocale(String locale) {
    state = locale;
  }
}

final localeProvider = NotifierProvider<LocaleNotifier, String>(LocaleNotifier.new);

extension StringLocalization on String {
  String toLocalizedRole(String langCode) {
    if (langCode == 'id') {
      switch (this) {
        case 'Hitchhiker':
          return 'Penumpang';
        case 'Driver':
          return 'Pemobil';
        case 'Rider':
          return 'Pemotor';
        case 'to_campus':
          return 'Ke Kampus';
        case 'from_campus':
          return 'Dari Kampus';
      }
    }
    // Default English or unrecognized string
    return this;
  }
}

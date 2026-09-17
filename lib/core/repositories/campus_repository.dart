class Campus {
  final String id;
  final String displayName;
  final String region;
  final List<String> geohashes;
  final List<String> keywords;

  const Campus({
    required this.id,
    required this.displayName,
    required this.region,
    required this.geohashes,
    required this.keywords,
  });
}

class CampusRepository {
  static const List<Campus> allCampuses = [
    // Surabaya
    Campus(
      id: 'UNAIR Kampus A',
      displayName: 'UNAIR Kampus A',
      region: 'Surabaya',
      geohashes: ['qw8ntvk5'],
      keywords: ['airlangga', 'unair'],
    ),
    Campus(
      id: 'UNAIR Kampus B',
      displayName: 'UNAIR Kampus B',
      region: 'Surabaya',
      geohashes: ['qw8ntuh'],
      keywords: ['airlangga', 'unair'],
    ),
    Campus(
      id: 'UNAIR Kampus C',
      displayName: 'UNAIR Kampus C',
      region: 'Surabaya',
      geohashes: ['qw8nwmr', 'qw8nwkz'],
      keywords: ['airlangga', 'unair'],
    ),
    Campus(
      id: 'ITS Kampus Sukolilo',
      displayName: 'ITS Kampus Sukolilo',
      region: 'Surabaya',
      geohashes: ['qw8nwer', 'qw8nwds', 'qw8nw9j'],
      keywords: ['institut sepuluh', 'sepuluh november', 'its'],
    ),
    Campus(
      id: 'ITS Kampus Manyar',
      displayName: 'ITS Kampus Manyar',
      region: 'Surabaya',
      geohashes: ['qw8ntcrz'],
      keywords: ['institut sepuluh', 'sepuluh november', 'its'],
    ),
    Campus(
      id: 'UNESA Kampus 1',
      displayName: 'UNESA Kampus 1',
      region: 'Surabaya',
      geohashes: ['qw8nmhh'],
      keywords: ['ketintang', 'universitas negeri surabaya', 'negeri surabaya', 'unesa'],
    ),
    Campus(
      id: 'UNESA Kampus 2',
      displayName: 'UNESA Kampus 2',
      region: 'Surabaya',
      geohashes: ['qw8n7yv'],
      keywords: ['lidah wetan', 'universitas negeri surabaya', 'negeri surabaya', 'unesa'],
    ),
    Campus(
      id: 'UNESA Kampus 3',
      displayName: 'UNESA Kampus 3',
      region: 'Surabaya',
      geohashes: ['qw8ntv63'],
      keywords: ['moestopo', 'universitas negeri surabaya', 'negeri surabaya', 'unesa'],
    ),
    // Yogyakarta
    Campus(
      id: 'UNY Pusat',
      displayName: 'UNY Pusat',
      region: 'Yogyakarta',
      geohashes: ['qqw7zbt'],
      keywords: ['universitas negeri yogyakarta', 'negeri yogyakarta', 'uny'],
    ),
    Campus(
      id: 'UNY Barat',
      displayName: 'UNY Barat',
      region: 'Yogyakarta',
      geohashes: ['qqw7zbe'],
      keywords: ['universitas negeri yogyakarta', 'negeri yogyakarta', 'uny'],
    ),
    Campus(
      id: 'UNY Utara',
      displayName: 'UNY Utara',
      region: 'Yogyakarta',
      geohashes: ['qqw7zcw5'],
      keywords: ['universitas negeri yogyakarta', 'negeri yogyakarta', 'uny'],
    ),
    Campus(
      id: 'UGM Pusat',
      displayName: 'UGM Pusat',
      region: 'Yogyakarta',
      geohashes: ['qqw7zc8'],
      keywords: ['universitas gajah mada', 'gajah mada', 'ugm'],
    ),
    Campus(
      id: 'UGM FT',
      displayName: 'UGM FT',
      region: 'Yogyakarta',
      geohashes: ['qqw7zd6'],
      keywords: ['teknik', 'universitas gajah mada', 'ugm'],
    ),
    Campus(
      id: 'UGM Vokasi',
      displayName: 'UGM Vokasi',
      region: 'Yogyakarta',
      geohashes: ['qqw7z8e9', 'qqw7z8ev', 'qqw7z8u1'],
      keywords: ['vokasi', 'universitas gajah mada', 'ugm'],
    ),
    Campus(
      id: 'UGM FK',
      displayName: 'UGM FK',
      region: 'Yogyakarta',
      geohashes: ['qqw7z9sg'],
      keywords: ['kedokteran', 'farmasi', 'universitas gajah mada', 'ugm'],
    ),
    Campus(
      id: 'UGM FMIPA',
      displayName: 'UGM FMIPA',
      region: 'Yogyakarta',
      geohashes: ['qqw7zdj'],
      keywords: ['biologi', 'mipa', 'universitas gajah mada', 'ugm'],
    ),
    Campus(
      id: 'ISI',
      displayName: 'ISI',
      region: 'Yogyakarta',
      geohashes: ['qqw7r4xt'],
      keywords: ['institut seni indonesia', 'isi'],
    ),
    // Jabodetabek
    Campus(
      id: 'UI FT',
      displayName: 'UI FT',
      region: 'Jabodetabek',
      geohashes: ['qqggycg'],
      keywords: ['teknik', 'feb', 'ekonomi bisnis', 'universitas indonesia', 'ui'],
    ),
    Campus(
      id: 'UI FIB',
      displayName: 'UI FIB',
      region: 'Jabodetabek',
      geohashes: ['qqggycm'],
      keywords: ['ilmu budaya', 'hukum', 'fisip', 'fh', 'universitas indonesia', 'ui'],
    ),
    Campus(
      id: 'UI FMIPA',
      displayName: 'UI FMIPA',
      region: 'Jabodetabek',
      geohashes: ['qqggybt'],
      keywords: ['komputer', 'fmipa', 'universitas indonesia', 'ui'],
    ),
  ];

  static List<Campus> searchCampuses(String query) {
    String normalized = query.toLowerCase().trim();
    if (normalized.isEmpty) return allCampuses;

    normalized = normalized.replaceAll(RegExp(r'\bkampus\b'), 'campus');
    normalized = normalized.replaceAll(RegExp(r'\bnegri\b'), 'negeri');
    normalized = normalized.replaceAll(RegExp(r'\buniversitas airlangga\b'), 'unair');

    List<String> tokens = normalized.split(' ').where((t) => t.isNotEmpty).toList();

    return allCampuses.where((campus) {
      String displayLower = campus.displayName.toLowerCase();
      
      // All tokens must be matched
      return tokens.every((token) {
        // Token matches if it's a substring of displayName
        if (displayLower.contains(token)) return true;
        // Or if it's a substring of ANY keyword
        return campus.keywords.any((keyword) => keyword.toLowerCase().contains(token));
      });
    }).toList();
  }
}

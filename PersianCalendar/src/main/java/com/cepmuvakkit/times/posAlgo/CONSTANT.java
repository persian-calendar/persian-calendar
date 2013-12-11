package com.cepmuvakkit.times.posAlgo;




public class CONSTANT {

	public static final boolean DEBUG = false;

	public static final long RESTART_DELAY = 1000; // 1 second
	public static final long POST_NOTIFICATION_DELAY = 5000; // 5 seconds

	public static final String[][] CALCULATION_METHOD_COUNTRY_CODES = new String[][]{


		/** TURKISH_RELIGOUS :		____ **/
		new String[]{"TRY"},
		/** METHOD_NORTH_AMERICA:	Parts of the USA, Canada, Parts of the UK **/
		new String[]{"USA", "CAN"},
		/** METHOD_MUSLIM_LEAGUE:	Europe, The Far East, Parts of the USA **/
		new String[]{
				// Europe
				"AND", "AUT", "BEL", "DNK", "FIN", "FRA", "DEU", "GIB", "IRL", "ITA", "LIE", "LUX", "MCO", "NLD", "NOR", "PRT", "SMR", "ESP", "SWE", "CHE", "GBR", "VAT",
				// Far East
				"CHN", "JPN", "KOR", "PRK","TRY", "TWN"
		},
		/** METHOD_EGYPT_SURVEY:	Africa, Syria, Iraq, Lebanon, Malaysia, Parts of the USA **/
		new String[]{
				// Africa
				"AGO", "BDI", "BEN", "BFA", "BWA", "CAF", "CIV", "CMR", "COG", "COM", "CPV", "DJI", "DZA", "EGY", "ERI", "ESH", "ETH", "GAB", "GHA", "GIN", "GMB", "GNB", "GNQ", "KEN", "LBR", "LBY", "LSO", "MAR", "MDG", "MLI", "MOZ", "MRT", "MUS", "MWI", "MYT", "NAM", "NER", "NGA", "REU", "RWA", "SDN", "SEN", "SLE", "SOM", "STP", "SWZ", "SYC", "TCD", "TGO", "TUN", "TZA", "UGA", "ZAF", "ZAR", "ZWB", "ZWE",
				// Syria, Iraq, Lebanon, Malaysia
				"IRQ", "LBN", "MYS", "SYR"
		},
		/** METHOD_KARACHI_HANAF:	Pakistan, Bangladesh, India, Afghanistan, Parts of Europe **/
		new String[]{"AFG", "BGD", "IND", "PAK"},

	
		
		/** METHOD_UMM_ALQURRA:		The Arabian Peninsula **/
		new String[]{"BHR", "KWT", "OMN", "QAT", "SAU", "YEM"},

		/** CUSTOM_DAWN_DUSK:		___ **/
		new String[]{}

	};
//	public static final Method[] CALCULATION_METHODS = new Method[]{Method.EGYPT_SURVEY, Method.KARACHI_SHAF, Method.KARACHI_HANAF, Method.NORTH_AMERICA, Method.MUSLIM_LEAGUE, Method.UMM_ALQURRA, Method.FIXED_ISHAA};
    public static final short DEFAULT_CALCULATION_METHOD = 0; // TURKISH_RELIGOUS 

	public static final short FAJR = 0, SUNRISE = 1, DHUHR = 2, ASR = 3, MAGHRIB = 4, ISHAA = 5, NEXT_FAJR = 6; // Notification Times
	//public static int[] TIME_NAMES = new int[]{R.string.fajr, R.string.sunrise, R.string.dhuhr, R.string.asr, R.string.maghrib, R.string.ishaa, R.string.next_fajr};

	public static final short NOTIFICATION_NONE = 0, NOTIFICATION_DEFAULT = 1, NOTIFICATION_PLAY = 2, NOTIFICATION_CUSTOM = 3; // Notification Methods

	public static final short DEFAULT_TIME_FORMAT = 0; // AM/PM

	//public static final Rounding[] ROUNDING_TYPES = new Rounding[]{Rounding.NONE, Rounding.NORMAL, Rounding.SPECIAL, Rounding.AGRESSIVE};
	public static final short DEFAULT_ROUNDING_TYPE = 2; // Special

	private CONSTANT() {
		// Private constructor to enforce un-instantiability.
	}
}
package com.github.wwadge.hbnpojogen.randomlib.data;

/**
 * Static country codes data.
 *
 * @author robertam
 */
public class CountryCodes {


    /**
     * Static numeric Data for Country Codes.
     */
    private static final String[] countryCodesNum = {
            "004", "008", "010", "012", "016", "020", "024", "028", "031", "032", "036", "040", "044", "048", "050", "051",
            "052", "056", "060", "064", "068", "070", "072", "074", "076", "084", "086", "090", "092", "096", "100", "104",
            "108", "112", "116", "120", "124", "132", "136", "140", "144", "148", "152", "156", "158", "162", "166", "170",
            "174", "175", "178", "180", "184", "188", "191", "192", "196", "203", "204", "208", "212", "214", "218", "222",
            "226", "231", "232", "233", "234", "238", "239", "242", "246", "248", "250", "254", "258", "260", "262", "266",
            "268", "270", "275", "276", "288", "292", "296", "300", "304", "308", "312", "316", "320", "324", "328", "332",
            "334", "336", "340", "344", "348", "352", "356", "360", "364", "368", "372", "376", "380", "384", "388", "392",
            "398", "400", "404", "408", "410", "414", "417", "418", "422", "426", "428", "430", "434", "438", "440", "442",
            "446", "450", "454", "458", "462", "466", "470", "474", "478", "480", "484", "492", "496", "498", "500", "504",
            "508", "512", "516", "520", "524", "528", "530", "533", "540", "548", "554", "558", "562", "566", "570", "574",
            "578", "580", "581", "583", "584", "585", "586", "591", "598", "600", "604", "608", "612", "616", "620", "624",
            "626", "630", "634", "638", "642", "643", "646", "654", "659", "660", "662", "666", "670", "674", "678", "682",
            "686", "690", "694", "702", "703", "704", "705", "706", "710", "716", "724", "732", "736", "740", "744", "748",
            "752", "756", "760", "762", "764", "768", "772", "776", "780", "784", "788", "792", "795", "796", "798", "800",
            "804", "807", "818", "826", "830", "833", "834", "840", "850", "854", "858", "860", "862", "876", "882", "887",
            "891", "894"
    };


    /**
     * Static acronym Data for Country Codes.
     */
    private static final String[] countryCodesAc = {
            "GB", "US", "CA", "MX", "BM", "SE", "IT", "A2", "PR", "IN", "VI", "DE",
            "IR", "BO", "NG", "NL", "FR", "IL", "ES", "PA", "CL", "BS", "AR", "DM",
            "BE", "IE", "BZ", "BR", "CH", "ZA", "EG", "SC", "TZ", "KM", "RW", "DZ",
            "GH", "CI", "SZ", "CM", "MG", "KE", "AO", "NA", "MA", "SL", "GA", "MU",
            "TG", "LY", "SN", "SD", "UG", "ZW", "MZ", "MW", "ZM", "BW", "LR", "CD",
            "BJ", "TN", "JP", "EU", "AU", "TH", "CN", "MY", "PK", "NZ", "KR", "HK",
            "SG", "BD", "ID", "PH", "TW", "AF", "VN", "VU", "NC", "BN", "AP", "GR",
            "SA", "PL", "CZ", "RU", "CY", "NO", "AT", "UA", "TJ", "DK", "PT", "TR",
            "GE", "BY", "IQ", "AM", "LB", "MD", "BG", "FI", "OM", "LV", "KZ", "EE",
            "SK", "JO", "HU", "KW", "AL", "LT", "SM", "RO", "RS", "HR", "LU", "IS",
            "LI", "CR", "MK", "MT", "GM", "SI", "FK", "AZ", "MC", "HT", "GU", "JM",
            "FM", "EC", "CO", "PE", "KY", "GP", "HN", "YE", "VG", "LC", "SY", "NI",
            "DO", "AN", "GT", "VE", "BA", "HM", "UY", "SV", "AE", "TT", "LK", "BV",
            "MH", "BH", "CK", "GI", "PY", "AG", "LS", "KN", "WS", "PW", "QA", "KH",
            "AI", "AS", "TC", "MP", "UZ", "MO", "UM", "RE", "GY", "CU", "CG", "A1",
            "BB", "LA", "SR", "AW", "FJ", "MS", "GD", "VC", "NP", "NE", "KG", "ME",
            "TD", "FO", "SO", "ML", "PS", "BI", "GN", "ET", "MR", "MQ", "VA", "TM",
            "YT", "BF", "AD", "AQ", "GL", "WF", "PG", "MN", "PF", "MV", "GQ", "CF",
            "ER", "GW", "DJ", "CV", "ST", "GF", "SB", "TV", "KI", "TO", "IO", "NU",
            "TK", "NR", "BT", "NF", "MM", "KP"
    };

    /**
     * Return the i'th element of the country codes ac array.
     *
     * @param i the element to return
     * @return Return the i'th element of the country codes ac array
     */
    public static String getCountryCodesAc(int i) {
        return getCountryCode(countryCodesAc, i);
    }

    /**
     * Return the i'th element of the country codes num array.
     *
     * @param i the element to return
     * @return Return the i'th element of the country codes num array
     */
    public static String getCountryCodesNum(int i) {
        return getCountryCode(countryCodesNum, i);
    }

    /**
     * Return the length of the country codes num array.
     *
     * @return the length of the country codes num array
     */
    public static int getCountryCodesNumLength() {
        return getCountryCodeLength(countryCodesNum);
    }

    /**
     * Return the length of the country codes ac array.
     *
     * @return the length of the country codes ac array
     */
    public static int getCountryCodesAcLength() {
        return getCountryCodeLength(countryCodesAc);
    }

    /**
     * Return a country code.
     *
     * @param i  index
     * @param ar array to use
     * @return a valid country code
     */
    public static String getCountryCode(String[] ar, int i) {
        if (i > ar.length) {
            throw new IndexOutOfBoundsException();
        }
        return ar[i];
    }

    /**
     * Return the length of the array.
     *
     * @param ar array to use
     * @return max no of country code array length
     */
    public static int getCountryCodeLength(String[] ar) {
        return ar.length;
    }
}

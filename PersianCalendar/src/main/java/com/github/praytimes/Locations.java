package com.github.praytimes;

public enum Locations {
    ARAK(34.08, 49.70), ARDABIL(38.25, 48.28), ORUMIYEH(37.53, 45d), ESFEHAN(
            32.65, 51.67), ALBORZ(35.82, 50.97), AHVAZ(31.52, 48.68), ILAM(
            33.63, 46.42), BOJNURD(37.47, 57.33), BANDAR_ABAS(27.18, 56.27), BUSHEHR(
            28.96, 50.84), BIRJAND(32.88, 59.22), TABRIZ(38.08, 46.3), TEHRAN(
            35.68, 51.42), KHORAM_ABAD(33.48, 48.35), RASHT(37.3, 49.63), ZAHEDAN(
            29.5, 60.85), ZANJAN(36.67, 48.48), SARI(36.55, 53.1), SEMNAN(
            35.57, 53.38), SANANDAJ(35.3, 47.02), SHAHREKORD(32.32, 50.85), SHIRAZ(
            29.62, 52.53), GHAZVIN(36.45, 50), GHOM(34.65, 50.95), KERMAN(
            30.28, 57.06), KERMANSHAH(34.32, 47.06), GORGAN(36.83, 54.48), MASHHAD(
            34.3, 59.57), HAMEDAN(34.77, 48.58), YASUJ(30.82, 51.68), YAZD(
            31.90, 54.37), KABUL(34.53, 69.16);

    private Coordinate coordinate;

    Locations(double latitude, double longitude) {
        coordinate = new Coordinate(latitude, longitude);
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }
}

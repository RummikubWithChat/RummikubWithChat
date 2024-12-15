package GUI;

// 타일 객체를 나타내는 클래스
class Tile {
    private int number;
    private TileColor color;

    public Tile(int number, TileColor color) {
        this.number = number;
        this.color = color;
    }

    public int getNumber() {
        return number;
    }

    public TileColor getColor() {
        return color;
    }
}
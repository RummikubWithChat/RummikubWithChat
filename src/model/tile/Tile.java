package model.tile;

public class Tile {
    public int number;
    public TileColor color;

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
    
    @Override
    public String toString() {
        return "[" + number + ", " + color + "]";
    }
}
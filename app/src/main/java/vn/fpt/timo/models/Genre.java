package vn.fpt.timo.models;

public class Genre {
    private String name;

    public Genre() {}

    public Genre(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}

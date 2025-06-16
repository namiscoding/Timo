package vn.fpt.timo.data.models;

public class ScreeningRoom {
    private String name;
    private long layoutRows;
    private long layoutCols;

    public ScreeningRoom(String name, long layoutRows, long layoutCols) {
        this.name = name;
        this.layoutRows = layoutRows;
        this.layoutCols = layoutCols;
    }

    public ScreeningRoom(){}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getLayoutRows() {
        return layoutRows;
    }

    public void setLayoutRows(long layoutRows) {
        this.layoutRows = layoutRows;
    }

    public long getLayoutCols() {
        return layoutCols;
    }

    public void setLayoutCols(long layoutCols) {
        this.layoutCols = layoutCols;
    }
}

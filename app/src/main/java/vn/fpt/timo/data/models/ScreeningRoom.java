package vn.fpt.timo.data.models; // Đảm bảo đúng package của bạn

import com.google.firebase.firestore.Exclude; // Import annotation @Exclude
import com.google.firebase.firestore.IgnoreExtraProperties; // Tùy chọn, nhưng tốt nếu bạn có các trường khác không muốn ánh xạ

import java.io.Serializable;
import java.util.Objects;

@IgnoreExtraProperties
public class ScreeningRoom  implements Serializable {

    private String id;
    private String name;
    private long layoutRows;
    private long layoutCols;


    public ScreeningRoom() {

    }


    public ScreeningRoom(String id, String name, long layoutRows, long layoutCols) {
        this.id = id;
        this.name = name;
        this.layoutRows = layoutRows;
        this.layoutCols = layoutCols;
    }


    public ScreeningRoom(String name, long layoutRows, long layoutCols) {
        this.name = name;
        this.layoutRows = layoutRows;
        this.layoutCols = layoutCols;
    }

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    @Exclude
    public long getTotalSeats() {
        return layoutRows * layoutCols;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScreeningRoom room = (ScreeningRoom) o;
        return layoutRows == room.layoutRows &&
                layoutCols == room.layoutCols &&
                Objects.equals(id, room.id) &&
                Objects.equals(name, room.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, layoutRows, layoutCols);
    }

    @Override
    public String toString() {
        return "ScreeningRoom{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", layoutRows=" + layoutRows +
                ", layoutCols=" + layoutCols +
                '}';
    }
}
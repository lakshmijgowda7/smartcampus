import java.util.List;

public interface ManagementOperations<T> {
    void addRecord(T record);

    List<T> getAllRecords();

    T findRecord(String id);

    boolean updateRecord(String id, T record);

    boolean deleteRecord(String id);
}

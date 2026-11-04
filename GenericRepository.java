import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class GenericRepository<T> {
    private final ArrayList<T> records = new ArrayList<>();
    private final Function<? super T, String> idProvider;

    public GenericRepository(Function<? super T, String> idProvider) {
        if (idProvider == null) {
            throw new IllegalArgumentException("An ID provider is required.");
        }
        this.idProvider = idProvider;
    }

    public void add(T record) {
        if (record == null) {
            throw new IllegalArgumentException("A record is required.");
        }
        records.add(record);
    }

    public List<T> getAll() {
        return new ArrayList<>(records);
    }

    public T findById(String id) {
        if (id == null) {
            return null;
        }
        String searchId = id.trim();
        return find(record -> idProvider.apply(record).equalsIgnoreCase(searchId));
    }

    public T find(Predicate<? super T> condition) {
        if (condition == null) {
            throw new IllegalArgumentException("A search condition is required.");
        }
        return records.stream().filter(condition).findFirst().orElse(null);
    }

    public boolean update(String id, T updatedRecord) {
        if (updatedRecord == null) {
            throw new IllegalArgumentException("Updated record details are required.");
        }
        if (id == null) {
            return false;
        }

        String searchId = id.trim();
        for (int index = 0; index < records.size(); index++) {
            if (idProvider.apply(records.get(index)).equalsIgnoreCase(searchId)) {
                records.set(index, updatedRecord);
                return true;
            }
        }
        return false;
    }

    public boolean remove(String id) {
        if (id == null) {
            return false;
        }
        String searchId = id.trim();
        for (int index = 0; index < records.size(); index++) {
            if (idProvider.apply(records.get(index)).equalsIgnoreCase(searchId)) {
                records.remove(index);
                return true;
            }
        }
        return false;
    }

    public void replaceAll(List<T> replacementRecords) {
        if (replacementRecords == null || replacementRecords.contains(null)) {
            throw new IllegalArgumentException("A valid record list is required.");
        }
        ArrayList<T> copy = new ArrayList<>(replacementRecords);
        records.clear();
        records.addAll(copy);
    }
}

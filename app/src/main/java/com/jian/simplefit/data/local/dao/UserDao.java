package com.jian.simplefit.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.jian.simplefit.data.local.entity.UserEntity;

import java.util.List;

/**
 * DAO (Data Access Object) cho việc truy cập dữ liệu người dùng trong cơ sở dữ liệu local
 */
@Dao
public interface UserDao {
    /**
     * Thêm một người dùng mới vào cơ sở dữ liệu
     * @param user Người dùng cần thêm
     * @return ID của người dùng đã thêm
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertUser(UserEntity user);

    /**
     * Thêm nhiều người dùng vào cơ sở dữ liệu
     * @param users Danh sách người dùng cần thêm
     * @return Danh sách ID của các người dùng đã thêm
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertUsers(List<UserEntity> users);

    /**
     * Cập nhật thông tin một người dùng đã tồn tại
     * @param user Người dùng cần cập nhật
     */
    @Update
    void updateUser(UserEntity user);

    /**
     * Xóa một người dùng khỏi cơ sở dữ liệu
     * @param user Người dùng cần xóa
     */
    @Delete
    void deleteUser(UserEntity user);

    /**
     * Lấy người dùng theo ID
     * @param userId ID của người dùng cần lấy
     * @return LiveData chứa người dùng tìm thấy hoặc null nếu không tồn tại
     */
    @Query("SELECT * FROM users WHERE id = :userId")
    LiveData<UserEntity> getUserById(String userId);

    /**
     * Lấy người dùng theo ID (không sử dụng LiveData - trả về ngay lập tức)
     * @param userId ID của người dùng cần lấy
     * @return Người dùng tìm thấy hoặc null nếu không tồn tại
     */
    @Query("SELECT * FROM users WHERE id = :userId")
    UserEntity getUserByIdSync(String userId);

    /**
     * Lấy người dùng theo email
     * @param email Email của người dùng cần lấy
     * @return LiveData chứa người dùng tìm thấy hoặc null nếu không tồn tại
     */
    @Query("SELECT * FROM users WHERE email = :email")
    LiveData<UserEntity> getUserByEmail(String email);

    /**
     * Kiểm tra email đã tồn tại chưa
     * @param email Email cần kiểm tra
     * @return true nếu email đã tồn tại, false nếu chưa
     */
    @Query("SELECT COUNT(*) > 0 FROM users WHERE email = :email")
    boolean isEmailRegistered(String email);

    /**
     * Lấy tất cả người dùng, sắp xếp theo tên
     * @return LiveData chứa danh sách tất cả người dùng
     */
    @Query("SELECT * FROM users ORDER BY displayName ASC")
    LiveData<List<UserEntity>> getAllUsers();

    /**
     * Tìm kiếm người dùng theo tên
     * @param query Chuỗi tìm kiếm
     * @return LiveData chứa danh sách người dùng phù hợp
     */
    @Query("SELECT * FROM users WHERE displayName LIKE '%' || :query || '%' ORDER BY displayName ASC")
    LiveData<List<UserEntity>> searchUsers(String query);

    /**
     * Thêm thường trình vào danh sách thường trình của người dùng
     * @param userId ID của người dùng
     * @param routineId ID của thường trình
     */
    @Query("UPDATE users SET routineIds = routineIds || ',' || :routineId WHERE id = :userId AND routineIds NOT LIKE '%' || :routineId || '%'")
    void addRoutineToUser(String userId, String routineId);

    /**
     * Xóa thường trình khỏi danh sách thường trình của người dùng
     * @param userId ID của người dùng
     * @param routineId ID của thường trình
     */
    @Query("UPDATE users SET routineIds = REPLACE(routineIds, ',' || :routineId, '') WHERE id = :userId")
    void removeRoutineFromUser(String userId, String routineId);

    /**
     * Cập nhật cân nặng của người dùng
     * @param userId ID của người dùng
     * @param weight Cân nặng mới
     * @param date Ngày cập nhật
     */
    @Query("UPDATE users SET currentWeight = :weight, lastWeightUpdateDate = :date WHERE id = :userId")
    void updateUserWeight(String userId, float weight, long date);

    /**
     * Xóa tất cả người dùng
     */
    @Query("DELETE FROM users")
    void deleteAllUsers();
}

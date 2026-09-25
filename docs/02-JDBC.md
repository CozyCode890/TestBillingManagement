# Java nói chuyện với MySQL như thế nào

Tài liệu này giải thích đúng thứ bạn đang thiếu: **cơ chế** Java gửi query xuống database và nhận kết quả về.

---

## 1. Bức tranh tổng thể

```
   Code của bạn
        |
        v
   java.sql.*          <-- JDBC: chỉ là INTERFACE, không có code thật
        |
        v
   mysql-connector-j.jar   <-- driver: bản cài đặt thật cho MySQL
        |
        v  (giao thức mạng MySQL, cổng 3306)
        |
   MySQL Server
```

Ba điều rút ra:

1. **Java không biết MySQL.** Nó chỉ biết bộ interface `java.sql.*` (Connection, Statement, ResultSet...). Bộ này giống hệt nhau dù bạn dùng MySQL, PostgreSQL hay Oracle.
2. **Driver là bản dịch.** File `.jar` trong thư mục `lib/` chứa code thật để nói chuyện với MySQL. Đổi sang PostgreSQL chỉ cần đổi file jar và chuỗi URL — code Java gần như giữ nguyên.
3. **Thiếu jar trong classpath** ⇒ lỗi kinh điển `No suitable driver found for jdbc:mysql://...`

> Từ JDBC 4.0 trở đi bạn **không cần** viết `Class.forName("com.mysql.jdbc.Driver")` nữa. Driver tự đăng ký khi có mặt trong classpath. Tutorial cũ nào còn dạy dòng đó là đã lỗi thời.

---

## 2. Sáu bước, không bao giờ thay đổi

Mọi lần truy vấn đều đi qua đúng các bước này. Xem `src/billing/db/QueryResult.java`, hàm `run()`, để thấy cả sáu bước nằm trong một hàm. (Comment trong file đó đánh số 5 bước vì gộp "thực thi" và "duyệt kết quả" làm một — cùng một việc, chỉ khác cách chia.)

```java
// BƯỚC 1 - Mở kết nối
Connection conn = DriverManager.getConnection(url, user, password);

// BƯỚC 2 - Chuẩn bị câu lệnh. Chỗ cần giá trị thì đặt dấu ?
String sql = "SELECT Price FROM Price_History WHERE Barcode = ? AND Valid_From_Date <= ?";
PreparedStatement ps = conn.prepareStatement(sql);

// BƯỚC 3 - Điền giá trị vào các dấu ?   (ĐÁNH SỐ TỪ 1, KHÔNG PHẢI 0)
ps.setString(1, "8934008");
ps.setDate(2, java.sql.Date.valueOf(LocalDate.now()));

// BƯỚC 4 - Thực thi
ResultSet rs = ps.executeQuery();      // SELECT      -> trả ResultSet
// int n   = ps.executeUpdate();       // INSERT/UPDATE/DELETE -> trả số dòng bị ảnh hưởng

// BƯỚC 5 - Duyệt kết quả
while (rs.next()) {                    // next() nhảy tới dòng kế, trả false khi hết
    BigDecimal price = rs.getBigDecimal("Price");
}

// BƯỚC 6 - Đóng (ngược thứ tự)
rs.close(); ps.close(); conn.close();
```

### `ResultSet` hoạt động ra sao

Hãy tưởng tượng một con trỏ đứng **trước** dòng đầu tiên:

```
          [con trỏ ở đây]
   ------------------------
   dòng 1 :  33000
   dòng 2 :  28000
```

`rs.next()` đẩy con trỏ xuống một bậc và trả `true` nếu còn dữ liệu. Vì vậy:

- Muốn lấy **nhiều dòng**: `while (rs.next()) { ... }`
- Muốn lấy **đúng một dòng**: `if (rs.next()) { ... }`
- **Chưa gọi `next()` mà đã `rs.getString(...)`** ⇒ lỗi `Before start of result set`. Đây là lỗi số 1 của người mới.

---

## 3. `try-with-resources` — đừng bao giờ tự gọi `close()`

Đoạn code ở mục 2 có một lỗ hổng: nếu `executeQuery()` ném exception thì `conn.close()` không bao giờ chạy. Kết nối bị rò rỉ. Rò đủ nhiều thì MySQL từ chối kết nối mới và cả hệ thống chết.

Cách viết đúng — mở tài nguyên trong ngoặc của `try`:

```java
try (Connection conn = Db.getConnection();
     PreparedStatement ps = conn.prepareStatement(sql)) {

    ps.setString(1, barcode);

    try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) return rs.getBigDecimal("Price");
    }
}   // Java TỰ gọi close() theo thứ tự ngược lại, kể cả khi có exception
```

Điều kiện: lớp đó phải implement `AutoCloseable` — `Connection`, `Statement`, `ResultSet` đều có.

---

## 4. `PreparedStatement` vs `Statement` — vì sao luôn chọn cái đầu

### Cách SAI

```java
String barcode = tfBarcode.getText();
String sql = "SELECT * FROM Product WHERE Barcode = '" + barcode + "'";
stmt.executeQuery(sql);
```

Nếu người dùng gõ vào ô nhập:

```
' OR '1'='1
```

Câu lệnh gửi xuống MySQL trở thành:

```sql
SELECT * FROM Product WHERE Barcode = '' OR '1'='1'
```

`'1'='1'` luôn đúng ⇒ trả về **toàn bộ bảng**. Với ô đăng nhập thì đây là đăng nhập không cần mật khẩu. Đây là **SQL Injection**, và nó vẫn đang đứng top các lỗ hổng web phổ biến nhất.

### Cách ĐÚNG

```java
PreparedStatement ps = conn.prepareStatement(
        "SELECT * FROM Product WHERE Barcode = ?");
ps.setString(1, barcode);
```

Câu lệnh và dữ liệu được gửi xuống MySQL **riêng biệt**. MySQL phân tích cấu trúc câu lệnh **trước**, sau đó mới nhét giá trị vào ô `?`. Giá trị đó luôn được hiểu là **dữ liệu**, không bao giờ là **lệnh**. Kẻ tấn công gõ gì cũng chỉ thành một chuỗi ký tự vô hại.

Lợi ích kèm theo: MySQL cache được kế hoạch thực thi, chạy lại nhiều lần nhanh hơn.

**Quy tắc:** giá trị đi vào SQL thì luôn qua `?`. Không có ngoại lệ.
(Tên bảng và tên cột *không* đặt được bằng `?` — nếu buộc phải động thì phải kiểm tra bằng whitelist.)

### 4.1. `CallableStatement` — gọi Function / Procedure có sẵn trong MySQL

`CallableStatement` là **con của `PreparedStatement`**: mọi thứ bạn vừa học (dấu `?`, đánh số từ 1, chống SQL Injection, `try-with-resources`) đều giữ nguyên. Nó chỉ thêm khả năng gọi các routine đã biên dịch sẵn trong database — và đây là cách tầng DAO của project này làm việc.

**Gọi Procedure trả về bảng dữ liệu** — cú pháp `{call sp_ten(?, ?)}`:

```java
try (Connection c = Db.getConnection();
     CallableStatement cs = c.prepareCall("{call sp_get_invoice_lines(?)}")) {

    cs.setString(1, invoiceId);          // vẫn đánh số từ 1

    try (ResultSet rs = cs.executeQuery()) {
        while (rs.next()) { ... }        // vẫn duyệt bằng rs.next()
    }
}
```

**Gọi Function trả về một giá trị đơn** — cú pháp `{? = call fn_ten(?, ?)}`. Dấu `?` đầu tiên là **giá trị trả về**, nên tham số đầu vào bắt đầu từ vị trí **2**:

```java
try (Connection c = Db.getConnection();
     CallableStatement cs = c.prepareCall("{? = call fn_get_product_price(?, ?)}")) {

    cs.registerOutParameter(1, Types.DECIMAL);   // 1 = chỗ nhận kết quả
    cs.setString(2, barcode);                    // tham số IN bắt đầu từ 2
    cs.setDate(3, java.sql.Date.valueOf(date));
    cs.execute();

    BigDecimal price = cs.getBigDecimal(1);      // đọc kết quả ở vị trí 1
}
```

Hai lỗi hay gặp nhất khi mới dùng:

- **Quên `registerOutParameter`** với Function ⇒ `Parameter number 1 is not an OUT parameter`.
- **Đánh số tham số IN từ 1** trong cú pháp `{? = call ...}` ⇒ giá trị bị nhét nhầm vào ô kết quả.

Code thật: `ProductDao.priceOn()` (Function) và `InvoiceDao.invoiceLines()` (Procedure). Lớp `QueryResult` có sẵn hàm `call()` gói trọn đoạn trên cho các thủ tục trả về bảng.

---

## 5. Transaction — được ăn cả, ngã về không

Lưu một hóa đơn không phải một lệnh, mà là nhiều lệnh:

```
INSERT Invoice        (1 dòng)
INSERT Invoice_Line   (N dòng)
INSERT Payment        (M dòng)
```

Nếu điện cúp giữa chừng, database còn lại một hóa đơn không có dòng hàng nào — dữ liệu rác không thể sửa tự động.

```java
Connection conn = Db.getConnection();
try {
    conn.setAutoCommit(false);     // MỞ transaction

    insertInvoice(conn, ...);      // chú ý: TRUYỀN CÙNG MỘT conn
    insertLines(conn, ...);
    insertPayments(conn, ...);

    conn.commit();                 // tất cả cùng có hiệu lực

} catch (SQLException e) {
    conn.rollback();               // tất cả cùng bị hủy, như chưa từng xảy ra
    throw e;
} finally {
    conn.setAutoCommit(true);
    conn.close();
}
```

Ba điểm dễ sai:

1. **Mặc định `autoCommit = true`** — mỗi câu lệnh tự commit ngay lập tức, không gộp nhóm được. Phải tắt đi bằng `setAutoCommit(false)`.
2. **Phải dùng chung một `Connection`.** Hai connection = hai transaction độc lập, `rollback()` ở cái này không ảnh hưởng cái kia. Đây là lỗi hay gặp khi mỗi DAO tự mở connection riêng.
3. **`rollback()` phải nằm trong `catch`**, và `setAutoCommit(true)` nằm trong `finally` trước khi đóng.

Xem code thật ở `src/billing/dao/InvoiceDao.java`, hàm `createInvoice()`. Ở đó ba lệnh `INSERT` đã được thay bằng ba lời gọi thủ tục (`sp_create_invoice_header`, `sp_add_invoice_line`, `sp_add_payment`), nhưng **ranh giới transaction vẫn do Java nắm** và cả ba đều chạy trên **cùng một `Connection`** — đúng điểm số 2 ở trên.

---

## 6. Kiểu dữ liệu: SQL ↔ Java

| Kiểu MySQL | Kiểu Java | Hàm đọc | Hàm ghi |
|---|---|---|---|
| `VARCHAR`, `TEXT`, `ENUM` | `String` | `getString()` | `setString()` |
| `INT` | `int` | `getInt()` | `setInt()` |
| `BIGINT` | `long` | `getLong()` | `setLong()` |
| `DECIMAL(12,2)` | **`BigDecimal`** | `getBigDecimal()` | `setBigDecimal()` |
| `DATE` | `java.sql.Date` | `getDate()` | `setDate()` |
| `TIME` | `java.sql.Time` | `getTime()` | `setTime()` |
| `DATETIME` | `java.sql.Timestamp` | `getTimestamp()` | `setTimestamp()` |
| `BOOLEAN` / `TINYINT(1)` | `boolean` | `getBoolean()` | `setBoolean()` |

### Tiền bạc: `BigDecimal`, không bao giờ `double`

```java
System.out.println(0.1 + 0.2);   // 0.30000000000000004
```

`double` lưu số theo hệ nhị phân, không biểu diễn chính xác được `0.1`. Cộng đủ nhiều lần thì báo cáo doanh thu lệch vài đồng — và kế toán sẽ hỏi tại sao.

```java
BigDecimal a = new BigDecimal("0.1");
BigDecimal b = new BigDecimal("0.2");
a.add(b);                        // đúng 0.3
```

Ba lưu ý về `BigDecimal`:

- **Khởi tạo từ `String`**, không từ `double`: `new BigDecimal(0.1)` vẫn dính sai số.
- **Bất biến**: `a.add(b)` **không** đổi `a`, nó trả về object mới. Phải viết `a = a.add(b)`.
- **So sánh bằng `compareTo()`**, không bằng `equals()`: `equals()` coi `1.0` khác `1.00`.

### `LocalDate` ↔ `java.sql.Date`

```java
LocalDate d = LocalDate.now();
ps.setDate(1, java.sql.Date.valueOf(d));        // Java -> SQL
LocalDate back = rs.getDate("Ngay").toLocalDate();  // SQL -> Java
```

### `NULL`

```java
ps.setNull(6, java.sql.Types.VARCHAR);   // ghi NULL xuống DB

String v = rs.getString("Customer_ID");
if (rs.wasNull()) { ... }                // kiểm tra giá trị vừa đọc có phải NULL không
```

Cẩn thận với số: `rs.getInt()` trả `0` khi gặp NULL, không phải `null`. Phải hỏi `rs.wasNull()` mới phân biệt được "bằng không" với "không có giá trị".

---

## 7. Phân tầng: vì sao không viết SQL thẳng trong nút bấm

Project này chia làm ba tầng:

```
billing/ui    Swing. Chỉ lo hiển thị và bắt sự kiện. KHÔNG có chữ SELECT nào.
     |
billing/dao   Điểm vào của mọi thao tác dữ liệu. Một lớp *Dao cho một nhóm bảng.
     |
billing/db    Mở/đóng kết nối, chạy câu lệnh, ghi log.
     |
   MySQL      sql/04_routines.sql -- nơi câu SQL thật sự nằm (fn_* và sp_*)
```

> **Một điểm project này khác sách giáo khoa:** phần lớn câu SQL **không nằm trong Java**. Chúng được đóng gói thành Stored Function (`fn_`) và Stored Procedure (`sp_`) trong `sql/04_routines.sql`, còn tầng DAO chỉ gọi chúng qua `CallableStatement` (xem mục 4.1 bên dưới). Lý do: bài tập này có hai nhóm làm song song — nhóm SQL sở hữu thư mục `sql/`, nhóm Java sở hữu `src/billing/`. Quy tắc chia việc và "hợp đồng" giữa hai bên nằm ở [05-SQL-JAVA-CONVENTION.md](05-SQL-JAVA-CONVENTION.md).
>
> Sáu bước ở mục 2 vẫn đúng nguyên vẹn — chỉ khác là câu lệnh gửi xuống có dạng `{call sp_...}` thay vì `SELECT ...`.

Lợi ích:

- Sửa một câu query chỉ cần mở đúng một file, không phải lục khắp các file giao diện.
- Muốn đổi Swing sang web (Spring Boot chẳng hạn) thì **giữ nguyên toàn bộ tầng dao**, chỉ viết lại tầng ui.
- Viết unit test cho DAO được, vì nó không cần cửa sổ nào cả.

Đây chính là kiến trúc mà Spring Boot dùng, chỉ khác là ở đó tầng `dao` gọi là Repository và Spring tự sinh code cho bạn.

---

## 8. Những lỗi bạn chắc chắn sẽ gặp

| Thông báo lỗi | Nguyên nhân | Cách sửa |
|---|---|---|
| `No suitable driver found` | Thiếu file jar trong classpath | Chạy `scripts\ps1\setup.ps1` (hoặc `scripts/bash/setup.sh`), kiểm tra tham số `-cp` |
| `Access denied for user 'root'@'localhost'` | Sai mật khẩu | Sửa `db.password` trong `config.properties` |
| `Unknown database 'retail_billing'` | Chưa tạo DB | Chạy `scripts\ps1\sql-create-db.ps1` (nạp `01_schema.sql` + `04_routines.sql`) |
| `Communications link failure` | MySQL chưa chạy / sai cổng | Bản MySQL portable của project **không** đăng ký Windows Service — chạy `scripts\ps1\mysql-start.ps1` hoặc bấm *Chạy SQL Server* trong `SetupApp.jar` |
| `Before start of result set` | Quên gọi `rs.next()` | Bọc trong `if`/`while (rs.next())` |
| `Cannot add or update a child row: foreign key constraint fails` | Ghi con trước khi có cha | Insert `Invoice` trước `Invoice_Line` |
| `Column 'X' in field list is ambiguous` | Hai bảng JOIN cùng có cột tên X | Ghi rõ `i.Invoice_ID` thay vì `Invoice_ID` |
| `Table 'retail_billing.return' doesn't exist` | `RETURN` là từ khóa MySQL | Bọc backtick: `` `Return` `` |
| `Parameter index out of range` | Số dấu `?` không khớp số lần `set...()` | Đếm lại, nhớ đánh số từ 1 |
| `PROCEDURE retail_billing.sp_xxx does not exist` | Chưa nạp `sql/04_routines.sql`, hoặc gõ sai tên thủ tục | Chạy lại `scripts\ps1\sql-create-db.ps1`; kiểm tra bằng `SHOW PROCEDURE STATUS WHERE Db='retail_billing';` |
| `Column 'San_pham' not found` | Dev SQL đổi alias trong thủ tục nhưng Java vẫn đọc tên cũ | Xem "Quy tắc 1: Khóa cứng tên cột" ở [05-SQL-JAVA-CONVENTION.md](05-SQL-JAVA-CONVENTION.md) |

---

## 9. Bước tiếp theo khi bạn đã hiểu phần này

1. **Connection pool (HikariCP).** Mở connection mới mỗi lần rất chậm (~50ms). Pool giữ sẵn 10 connection và cho mượn. Đây là thứ đầu tiên nên thêm vào dự án thật.
2. **Spring JdbcTemplate.** Bỏ được đống boilerplate, nhưng vẫn là SQL thật do bạn viết. Bước chuyển tiếp tự nhiên nhất từ JDBC thuần.
3. **JPA / Hibernate.** Sinh SQL tự động từ class Java. Mạnh nhưng che giấu nhiều thứ — nắm chắc JDBC trước rồi hãy dùng, nếu không sẽ không debug nổi khi nó sinh ra query chậm.

---

## Liên quan

- [01-NORMALIZATION.md](01-NORMALIZATION.md) — vì sao database lại chia nhiều bảng như vậy
- [03-SCHEMA.md](03-SCHEMA.md) — giải thích từng bảng
- [05-SQL-JAVA-CONVENTION.md](05-SQL-JAVA-CONVENTION.md) — quy chuẩn phân chia và giao tiếp giữa Dev Java và Dev SQL

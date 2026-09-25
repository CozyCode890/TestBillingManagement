# Cẩm nang Dev SQL: Nối MySQL Workbench, viết code theo I-P-O-T, test từng phần rồi ráp vào source

> **Dành cho:** Dev SQL (người sở hữu thư mục `sql/`) từng trải qua cảm giác: *viết một mạch 80 dòng stored procedure, chạy `db-setup`, bật app lên — và nhận về `Column 'San_pham' not found` hoặc một bảng báo cáo trống trơn, không biết sai ở SQL, ở alias, ở JDBC hay ở JTable.*
>
> **Mục tiêu:** Biến MySQL Workbench thành **phòng thí nghiệm** của bạn. Mỗi routine được xác định rõ **Input – Processing – Output – Test (I-P-O-T)** trước khi gõ, được test từng mảnh nhỏ ngay trong Workbench chỉ mất vài giây, rồi mới đóng băng vào `sql/04_routines.sql` và bàn giao cho Dev Java.
>
> **Tài liệu song sinh:** [`10-HUONG-DAN-TEST-TUNG-PHAN-CHO-JAVA-DEV.md`](10-HUONG-DAN-TEST-TUNG-PHAN-CHO-JAVA-DEV.md) dạy điều tương tự cho phía Java. Hai bên gặp nhau ở [`05-SQL-JAVA-CONVENTION.md`](05-SQL-JAVA-CONVENTION.md) — bản hợp đồng chung.

---

## MỤC LỤC

1. [Vì sao Dev SQL không thể viết một lèo](#1-vì-sao-dev-sql-không-thể-viết-một-lèo)
   - [1.1. Vòng lặp phản hồi của Dev SQL](#11-vòng-lặp-phản-hồi-của-dev-sql)
   - [1.2. Bốn chữ cái cứu mạng I-P-O-T](#12-bốn-chữ-cái-cứu-mạng-i-p-o-t)
2. [Nối MySQL Workbench vào database của dự án](#2-nối-mysql-workbench-vào-database-của-dự-án)
   - [2.1. Cài Workbench và bật server trước](#21-cài-workbench-và-bật-server-trước)
   - [2.2. Tạo Connection lấy thông số từ config.properties](#22-tạo-connection-lấy-thông-số-từ-configproperties)
   - [2.3. Kiểm tra bạn đang nối đúng server](#23-kiểm-tra-bạn-đang-nối-đúng-server)
   - [2.4. Bản đồ màn hình Workbench cho người mới](#24-bản-đồ-màn-hình-workbench-cho-người-mới)
   - [2.5. Bảng lỗi kết nối thường gặp](#25-bảng-lỗi-kết-nối-thường-gặp)
   - [2.6. Không cài được Workbench thì dùng gì](#26-không-cài-được-workbench-thì-dùng-gì)
3. [Trước khi gõ SQL hãy viết hợp đồng I-P-O-T](#3-trước-khi-gõ-sql-hãy-viết-hợp-đồng-i-p-o-t)
4. [Vòng lặp 7 bước viết một routine trong Workbench](#4-vòng-lặp-7-bước-viết-một-routine-trong-workbench)
5. [Bộ đồ nghề test từng phần ngay trong Workbench](#5-bộ-đồ-nghề-test-từng-phần-ngay-trong-workbench)
   - [5.1. Biến phiên làm tham số giả](#51-biến-phiên-làm-tham-số-giả)
   - [5.2. Bảng ca test PASS FAIL bằng UNION ALL](#52-bảng-ca-test-pass-fail-bằng-union-all)
   - [5.3. Test thủ tục trả về bảng dữ liệu](#53-test-thủ-tục-trả-về-bảng-dữ-liệu)
   - [5.4. Test thủ tục ghi dữ liệu mà không làm bẩn database](#54-test-thủ-tục-ghi-dữ-liệu-mà-không-làm-bẩn-database)
   - [5.5. Test ca lỗi và cách báo lỗi tử tế cho Java](#55-test-ca-lỗi-và-cách-báo-lỗi-tử-tế-cho-java)
   - [5.6. Test đối chiếu chéo](#56-test-đối-chiếu-chéo)
   - [5.7. Test hiệu năng bằng EXPLAIN](#57-test-hiệu-năng-bằng-explain)
6. [Ráp routine vào source code theo 6 bước](#6-ráp-routine-vào-source-code-theo-6-bước)
7. [Bài thực hành trọn vẹn viết fn_invoice_total](#7-bài-thực-hành-trọn-vẹn-viết-fn_invoice_total)
8. [13 cái bẫy đặc thù của Workbench và routine MySQL](#8-13-cái-bẫy-đặc-thù-của-workbench-và-routine-mysql)
9. [Bảng đối chiếu Dev SQL viết một lèo và Dev SQL I-P-O-T](#9-bảng-đối-chiếu-dev-sql-viết-một-lèo-và-dev-sql-i-p-o-t)
10. [Checklist dán trước bàn làm việc](#10-checklist-dán-trước-bàn-làm-việc)

---

## 1. Vì sao Dev SQL không thể viết một lèo

Quy trình mà gần như ai cũng từng làm ít nhất một lần:

```
[Đọc yêu cầu: "làm báo cáo doanh thu theo ngày"]
        ↓
[Mở 04_routines.sql, gõ một mạch 80 dòng JOIN + GROUP BY]
        ↓
[Chạy db-setup.ps1 nạp lại toàn bộ database]                      <-- 40 giây
        ↓
[Biên dịch Java, bật app Swing, đăng nhập, bấm sang tab Báo cáo]  <-- 60 giây
        ↓
💥 Một trong bốn kết cục:
   1. "Error Code: 1064 ... error in your SQL syntax"  (sai cú pháp, chưa chạy nổi)
   2. Bảng trên giao diện trống trơn                    (chạy được nhưng 0 dòng)
   3. "Column 'Doanh_thu' not found"                    (đổi alias, Java đọc không ra)
   4. Số tiền hiện ra đầy đủ nhưng SAI
```

Kết cục thứ tư là nguy hiểm nhất: **SQL sai vẫn chạy êm ru.** Java không bao giờ báo lỗi khi bạn `JOIN` thiếu một điều kiện làm nhân đôi số dòng, hay khi bạn quên `- Discount` trong công thức tính tiền. Chỉ có bạn — người viết câu SQL — mới đủ khả năng phát hiện, và chỉ phát hiện được nếu bạn **chủ động kiểm chứng từng mảnh**.

### 1.1. Vòng lặp phản hồi của Dev SQL

> **Feedback Loop:** khoảng thời gian từ lúc bạn gõ một mệnh đề SQL đến lúc bạn **biết chắc** mệnh đề đó cho ra đúng con số.

```
Quy trình "viết một lèo" (chậm và mù):
[Gõ 80 dòng SQL] -> [Nạp lại DB 40s] -> [Bật app 60s] -> [Bấm chuột] -> [Nhìn bảng] -> ???
└──────────────────────── Feedback Loop = 15 đến 30 phút ────────────────────────┘

Quy trình Workbench (nhanh và nhìn tận mắt):
[Gõ 1 mệnh đề] -> [Ctrl+Enter] -> [Nhìn Result Grid: 0.01s] -> [Đúng rồi, gõ tiếp]
└──── Feedback Loop = 5 giây ────┘
```

Dev SQL có một lợi thế mà Dev Java phải thèm: **SQL đã có sẵn môi trường chạy thử tương tác ngay trong Workbench.** Dev Java phải cài JUnit, phải biên dịch, phải viết class test mới chạy thử được một hàm. Bạn chỉ cần bôi đen một mệnh đề và bấm `Ctrl+Enter`. Không tận dụng thì quá phí.

### 1.2. Bốn chữ cái cứu mạng I-P-O-T

Trước khi gõ dòng SQL đầu tiên, hãy trả lời xong 4 câu hỏi:

```
+---+---------------+--------------------------------------------------------------+
| I | INPUT         | Routine nhận vào cái gì? Tên tham số, kiểu dữ liệu, có cho   |
|   | (Đầu vào)     | phép NULL không? Ví dụ: p_barcode VARCHAR(20), p_date DATE    |
+---+---------------+--------------------------------------------------------------+
| P | PROCESSING    | Đi qua những bảng nào, JOIN theo khóa gì, lọc điều kiện gì,   |
|   | (Xử lý)       | gom nhóm theo cột nào, sắp xếp ra sao?                        |
+---+---------------+--------------------------------------------------------------+
| O | OUTPUT        | Trả GIÁ TRỊ ĐƠN (fn_) hay BẢNG (sp_)? Tên cột (alias) là gì,  |
|   | (Đầu ra)      | kiểu gì? Không có dữ liệu thì trả NULL, trả 0, hay bảng rỗng? |
+---+---------------+--------------------------------------------------------------+
| T | TEST          | Liệt kê sẵn các ca: ca thường, ca rỗng, ca NULL, ca biên      |
|   | (Kiểm thử)    | ngày tháng, ca mã không tồn tại. Mỗi ca kỳ vọng ra số nào?    |
+---+---------------+--------------------------------------------------------------+
```

Viết 4 dòng này mất **5 phút**. Bỏ qua nó thường mất **2 tiếng** để dò lại vì sao con số trên giao diện lệch 1.000 đồng.

---

## 2. Nối MySQL Workbench vào database của dự án

### 2.1. Cài Workbench và bật server trước

Dự án này dùng **MySQL bản ZIP portable** (`%USERPROFILE%\mysql8\mysql-8.0.45-winx64`) nên **không có sẵn Workbench**. Cài riêng:

```bash
winget install Oracle.MySQLWorkbench
```

Hoặc tải bản cài tại <https://dev.mysql.com/downloads/workbench/>. Trên macOS: `brew install --cask mysqlworkbench`.

> ⚠️ **Workbench không tự bật server giúp bạn.** Bản portable không đăng ký Windows Service, nên nếu `mysqld` chưa chạy thì Workbench chỉ báo `Can't connect to MySQL server on '127.0.0.1' (10061)`. **Luôn bật server trước:**
>
> - Bấm đúp `Setup.bat` (hoặc `SetupApp.jar`) → tab **Chung** → nút bật MySQL, hoặc
> - Chạy lệnh sau và **để nguyên cửa sổ đó mở** trong lúc làm việc:

```bash
powershell -ExecutionPolicy Bypass -File .\scripts\ps1\mysql-start.ps1
```

### 2.2. Tạo Connection lấy thông số từ config.properties

Đừng đoán thông số kết nối. Mở [`config.properties`](../config.properties) ở thư mục gốc — đó là nguồn sự thật duy nhất mà cả app Java lẫn bạn đều phải dùng:

```properties
db.url=jdbc:mysql://localhost:3306/retail_billing?useSSL=false&allowPublicKeyRetrieval=true&...
db.user=root
db.password=root
```

Dịch sang ô nhập của Workbench (màn hình chính → bấm dấu `+` cạnh **MySQL Connections**):

| Ô trong Workbench | Điền giá trị | Lấy từ đâu |
|---|---|---|
| **Connection Name** | `RetailBilling - Local` | Tên tự đặt, chỉ để bạn nhận ra |
| **Connection Method** | `Standard (TCP/IP)` | Mặc định |
| **Hostname** | `127.0.0.1` | phần `localhost` trong `db.url` |
| **Port** | `3306` | phần `:3306` trong `db.url` |
| **Username** | `root` | `db.user` |
| **Password** | `root` → bấm **Store in Vault** | `db.password` |
| **Default Schema** | `retail_billing` | phần `/retail_billing` trong `db.url` |

Bấm **Test Connection**, thấy `Successfully made the MySQL connection` là xong, bấm **OK** để lưu. Lần sau chỉ cần bấm đúp vào ô connection.

> 💡 **Vì sao khuyên `127.0.0.1` thay vì `localhost`?** Trên Windows, `localhost` đôi khi được phân giải thành địa chỉ IPv6 `::1` và bạn gặp lỗi kết nối rất khó hiểu dù server vẫn chạy. Chính các script trong `scripts/ps1/` cũng đều dùng `-h 127.0.0.1`.
>
> **Mật khẩu `root`/`root`** chỉ chấp nhận được vì đây là database học tập chạy trên localhost. Đừng mang cách đặt mật khẩu này sang bất cứ hệ thống thật nào.

### 2.3. Kiểm tra bạn đang nối đúng server

Bước này mất 10 giây nhưng cứu bạn khỏi một buổi chiều mất trí, vì **một máy có thể cài nhiều bản MySQL cùng lúc**. Chính chiếc máy đang chạy dự án này có cả thư mục `C:\Program Files\MySQL\MySQL Server 8.4`, trong khi cổng 3306 lại do bản portable 8.0.45 phục vụ. Nếu Workbench lỡ nối sang bản kia, bạn sẽ sửa routine trong một database **không ai dùng** và không hiểu vì sao app vẫn chạy y như cũ.

Mở SQL Editor mới (`Ctrl+T`) và chạy:

```sql
SELECT VERSION()   AS phien_ban,
       @@port      AS cong,
       DATABASE()  AS schema_dang_chon,
       @@datadir   AS thu_muc_du_lieu,
       USER()      AS dang_dang_nhap;
```

Kết quả đúng: `8.0.45`, cổng `3306`, schema `retail_billing`, và `thu_muc_du_lieu` trỏ vào `mysql8\mysql-8.0.45-winx64\data\`. Nếu `schema_dang_chon` là `NULL` thì chạy thêm `USE retail_billing;`.

Tiếp theo, kiểm tra database đã nạp đủ chưa:

```sql
SELECT COUNT(*) AS so_bang
FROM   information_schema.tables
WHERE  table_schema = 'retail_billing';          -- kỳ vọng: 16

SELECT ROUTINE_TYPE, COUNT(*) AS so_luong
FROM   information_schema.routines
WHERE  routine_schema = 'retail_billing'
GROUP  BY ROUTINE_TYPE;                          -- kỳ vọng: FUNCTION 3, PROCEDURE 23
```

Số không khớp nghĩa là database chưa nạp đủ — chạy lại:

```bash
powershell -ExecutionPolicy Bypass -File .\scripts\ps1\db-setup.ps1
```

### 2.4. Bản đồ màn hình Workbench cho người mới

```
+---------------------------------------------------------------------------------+
| [Thanh công cụ]  ⚡ Execute         ⚡▾ Execute Current   ⟲ Toggle Autocommit    |
|                  (Ctrl+Shift+Enter)   (Ctrl+Enter)        [Limit to 1000 rows ▾] |
+----------------------+----------------------------------------------------------+
| NAVIGATOR            |  QUERY TAB  (nơi bạn gõ SQL)                              |
|  Schemas             |    SELECT * FROM Invoice_Line WHERE Invoice_ID='INV-0001';|
|   retail_billing     |                                                          |
|    ├ Tables (16)     +----------------------------------------------------------+
|    ├ Views           |  RESULT GRID  (bảng kết quả — xem TÊN CỘT ở đây!)         |
|    ├ Stored Procedures|   Invoice_ID | Line_Number | Barcode | Quantity | ...    |
|    └ Functions        |   INV-0001   | 1           | 8934001 | 2.000    | ...    |
+----------------------+----------------------------------------------------------+
|  ACTION OUTPUT: 12:03:41   SELECT * FROM ...   3 row(s) returned   0.015 sec     |
+---------------------------------------------------------------------------------+
```

Bốn thao tác phải thuộc lòng:

| Thao tác | Phím tắt | Khi nào dùng |
|---|---|---|
| Chạy **mệnh đề đang đứng con trỏ** | `Ctrl + Enter` | 90% thời gian: thử từng mảnh nhỏ |
| Chạy **cả tab hoặc phần bôi đen** | `Ctrl + Shift + Enter` | Chạy cả kịch bản test, hoặc khối `DELIMITER` |
| Mở tab SQL mới | `Ctrl + T` | Mỗi routine một tab, đừng trộn lẫn |
| Xem số dòng và thời gian chạy | Khung **Action Output** dưới cùng | Đo hiệu năng, kiểm tra số dòng trả về |

> Trong Navigator, chuột phải một routine → **Send to SQL Editor → Create Statement** sẽ dán toàn bộ mã nguồn routine đó ra tab để bạn đọc và sửa. Nhưng xem [bẫy số 12](#8-13-cái-bẫy-đặc-thù-của-workbench-và-routine-mysql) — **sửa ở đây thôi là chưa đủ**, luôn phải chép về `sql/04_routines.sql`.

### 2.5. Bảng lỗi kết nối thường gặp

| Thông báo lỗi | Nguyên nhân thật | Cách xử lý |
|---|---|---|
| `Can't connect to MySQL server on '127.0.0.1' (10061)` — Error 2003 | `mysqld` chưa chạy | Chạy `mysql-start.ps1` hoặc tab **Chung** của SetupApp; để cửa sổ đó mở |
| `Access denied for user 'root'@'localhost' (using password: YES)` — Error 1045 | Sai mật khẩu | Đối chiếu đúng `db.password` trong `config.properties` (dự án dùng `root`) |
| `Unknown database 'retail_billing'` — Error 1049 | Chưa nạp schema | Chạy `db-setup.ps1`, hoặc tab **SQL** của SetupApp → *Tạo database* |
| `Public Key Retrieval is not allowed` | Client chưa được phép lấy khóa công khai | Connection → tab **Advanced** → thêm `allowPublicKeyRetrieval=true` (chính là tham số đã có trong `db.url`) |
| `Authentication plugin 'caching_sha2_password' cannot be loaded` | Workbench quá cũ so với MySQL 8 | Cập nhật Workbench lên bản 8.0 trở lên |
| Kết nối được nhưng **không thấy bảng nào** | Đang đứng ở schema khác | Bấm đúp `retail_billing` trong Navigator cho nó **in đậm**, hoặc chạy `USE retail_billing;` |
| Chữ tiếng Việt hiện thành `?????` | Sai bộ ký tự | Connection → **Advanced** → `Default Character Set: utf8mb4` |

### 2.6. Không cài được Workbench thì dùng gì

**1. `mysql` CLI có sẵn trong bản portable** (không phải cài thêm gì):

```bash
& "$env:USERPROFILE\mysql8\mysql-8.0.45-winx64\bin\mysql.exe" -u root -p -h 127.0.0.1 -P 3306 -t --default-character-set=utf8mb4 retail_billing
```

Cờ `-t` bật chế độ vẽ bảng có khung, nhìn gần giống Result Grid của Workbench. Nạp một file kịch bản bằng `source` (viết tắt là `\.`):

```sql
mysql> source D:/DemoBillingManagement/sql/04_routines.sql
```

> Bên trong `mysql` CLI phải dùng **dấu gạch chéo xuôi** `/` trong đường dẫn, kể cả trên Windows.

**2. Tab SQL của `SetupApp.jar`** — bốn nút gói sẵn script: *Tạo database* (chạy `01_schema.sql` + `04_routines.sql`), *Tạo dữ liệu mẫu* (`02_seed.sql`), *Chạy 03_queries.sql*, *Xoá database*. Rất tiện để **nạp lại**, nhưng không phải chỗ để **thử nghiệm từng mệnh đề**.

**3. DBeaver** (miễn phí, đa nền tảng) hoặc extension MySQL của VS Code — cùng thông số ở mục [2.2](#22-tạo-connection-lấy-thông-số-từ-configproperties). Mọi kỹ thuật test ở mục 5 đều áp dụng y hệt.

---

## 3. Trước khi gõ SQL hãy viết hợp đồng I-P-O-T

Dán mẫu này lên đầu tab Workbench dưới dạng comment, điền xong rồi mới gõ SQL:

```sql
/* =====================================================================
   HỢP ĐỒNG ROUTINE
   Tên            : fn_... / sp_...
   Loại           : FUNCTION (trả 1 giá trị) | PROCEDURE (trả bảng / ghi dữ liệu)
   INPUT          : p_xxx KIỂU  -- cho phép NULL?  giá trị mẫu để test?
   PROCESSING     : bảng nào, JOIN gì, lọc gì, GROUP BY gì, ORDER BY gì
   OUTPUT         : tên cột (alias) + kiểu; không có dữ liệu thì trả gì?
   TEST           : 1) ca thường  2) ca rỗng  3) ca NULL  4) ca biên
   AI GỌI         : DAO nào bên Java sẽ dùng routine này
   ===================================================================== */
```

Ví dụ điền sẵn cho một routine **có thật** trong dự án — `fn_get_product_price`, trái tim của bài toán lịch sử giá:

| Mục | Nội dung |
|---|---|
| **Tên / Loại** | `fn_get_product_price` — FUNCTION, trả `DECIMAL(12,2)` |
| **INPUT** | `p_barcode VARCHAR(20)`, `p_date DATE`. Cả hai có thể bị truyền NULL từ Java |
| **PROCESSING** | Bảng `Price_History`, lọc `Barcode = p_barcode AND Valid_From_Date <= p_date`, sắp `ORDER BY Valid_From_Date DESC`, lấy `LIMIT 1` |
| **OUTPUT** | Một số tiền. **Không tìm thấy giá thì trả `NULL`**, không trả 0 |
| **TEST** | 1) giá hôm nay của `8934008` = 158000 · 2) giá 400 ngày trước = 130000 · 3) ngày trước khi có giá đầu tiên → NULL · 4) barcode không tồn tại → NULL · 5) tham số NULL → NULL |
| **AI GỌI** | `ProductDao.priceOn()` → hiển thị đơn giá khi thu ngân quét hàng |

Hai điều trong bảng trên là **cam kết với Dev Java**, đổi là vỡ app:

1. **Trả `NULL` chứ không trả `0`.** Java đang viết `price == null` để hiểu là "sản phẩm chưa có giá trước ngày đó". Nếu bạn sửa thành `COALESCE(v_price, 0)` cho "đẹp", app sẽ bán hàng giá 0 đồng mà không ai biết.
2. **Thứ tự tham số.** JDBC gắn tham số theo **vị trí dấu `?`**, không theo tên. Thêm tham số mới phải thêm vào **cuối danh sách**.

> Toàn bộ quy tắc hợp đồng nằm ở [`05-SQL-JAVA-CONVENTION.md`](05-SQL-JAVA-CONVENTION.md) mục 5. Bảng tra 26 routine ở mục 6 của tài liệu đó chính là **chỗ Dev Java nhìn đầu tiên** — thêm routine mới thì nhớ thêm một dòng vào đấy.

---

## 4. Vòng lặp 7 bước viết một routine trong Workbench

```
B0 Khám phá dữ liệu thật      -> biết bảng có gì, bao nhiêu dòng
B1 Viết SELECT trần chạy được -> dùng biến phiên thay cho tham số
B2 Kiểm chứng ngược           -> tính lại bằng cách khác, hai số phải khớp
B3 Bọc thành routine          -> DELIMITER $$ ... END$$
B4 Chạy bảng ca test          -> cột Ket_qua phải toàn PASS
B5 Soi hiệu năng              -> EXPLAIN, xem có quét toàn bảng không
B6 Đóng băng vào 04_routines.sql + cập nhật bảng tra ở docs/05
```

**B0 — Khám phá trước khi viết.** Đừng viết SQL cho một bảng mà bạn chưa nhìn thấy dữ liệu:

```sql
DESCRIBE Invoice_Line;                       -- có những cột gì, kiểu gì
SELECT COUNT(*) FROM Invoice_Line;           -- có bao nhiêu dòng
SELECT * FROM Invoice_Line LIMIT 10;         -- dữ liệu thật trông ra sao
SHOW CREATE TABLE Invoice_Line;              -- khóa chính, khóa ngoại, index
```

**B1 — SELECT trần với tham số giả.** Viết bằng biến phiên, chưa vội bọc routine (xem [5.1](#51-biến-phiên-làm-tham-số-giả)).

**B2 — Kiểm chứng ngược.** Một câu SQL cho ra số không có nghĩa là số đó đúng. Tính lại bằng đường khác rồi so (xem [5.6](#56-test-đối-chiếu-chéo)).

**B3 — Bọc thành routine.** Đây là lúc cần `DELIMITER`:

```sql
DROP FUNCTION IF EXISTS fn_vi_du;

DELIMITER $$
CREATE FUNCTION fn_vi_du(p_x INT)
RETURNS INT
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE v_kq INT DEFAULT NULL;
    SELECT p_x * 2 INTO v_kq;
    RETURN v_kq;
END$$
DELIMITER ;
```

> **`DELIMITER` là chuyện của client, không phải của MySQL server.** Bình thường Workbench thấy dấu `;` là cắt ra một câu lệnh gửi đi. Nhưng thân routine có đầy dấu `;` bên trong `BEGIN ... END`, nên phải tạm đổi dấu kết thúc câu sang `$$`, viết xong trả lại `;`. Khối này bắt buộc chạy bằng `Ctrl + Shift + Enter` (chạy cả khối); bấm `Ctrl + Enter` giữa chừng sẽ gửi đi một mảnh vỡ và nhận về lỗi 1064.

**B4 → B6** là toàn bộ mục [5](#5-bộ-đồ-nghề-test-từng-phần-ngay-trong-workbench) và [6](#6-ráp-routine-vào-source-code-theo-6-bước) dưới đây.

---

## 5. Bộ đồ nghề test từng phần ngay trong Workbench

### 5.1. Biến phiên làm tham số giả

Vấn đề kinh điển: routine chưa tồn tại thì lấy gì để thử `p_barcode`? Câu trả lời là **biến phiên** (`@ten_bien`) — nó sống suốt phiên kết nối và dùng được trong mọi câu lệnh:

```sql
SET @p_barcode := '8934008';
SET @p_date    := CURDATE();

-- Đây chính là phần thân tương lai của fn_get_product_price,
-- nhưng đang chạy trần nên bấm Ctrl+Enter là thấy kết quả ngay.
SELECT Price
FROM   Price_History
WHERE  Barcode = @p_barcode
  AND  Valid_From_Date <= @p_date
ORDER  BY Valid_From_Date DESC
LIMIT  1;
```

Đổi `SET @p_date := CURDATE() - INTERVAL 400 DAY;` rồi chạy lại — bạn vừa test ca biên "giá cũ" trong 2 giây, chưa cần tạo function nào cả.

> **Mẹo:** giữ nguyên tên biến trùng tên tham số tương lai (`@p_barcode` ↔ `p_barcode`). Khi bọc thành routine, bạn chỉ việc xóa ký tự `@` là xong, không phải sửa logic.

### 5.2. Bảng ca test PASS FAIL bằng UNION ALL

Đây là kỹ thuật đáng giá nhất của cả tài liệu, tương đương JUnit của Dev Java: **một câu SELECT duy nhất chạy hết mọi ca test và tự chấm điểm.**

Công thức mỗi dòng: `tên ca` · `giá trị thực tế` · `giá trị mong đợi` · `IF(so sánh, 'PASS', 'FAIL')`.

```sql
SELECT 'Gia hom nay cua 8934008' AS Ca_test,
       fn_get_product_price('8934008', CURDATE())                   AS Thuc_te,
       158000.00                                                    AS Mong_doi,
       IF(fn_get_product_price('8934008', CURDATE()) = 158000.00,
          'PASS', 'FAIL')                                           AS Ket_qua
UNION ALL
SELECT 'Gia 400 ngay truoc',
       fn_get_product_price('8934008', CURDATE() - INTERVAL 400 DAY),
       130000.00,
       IF(fn_get_product_price('8934008', CURDATE() - INTERVAL 400 DAY) = 130000.00,
          'PASS', 'FAIL')
UNION ALL
SELECT 'Truoc ngay co gia dau tien (ky vong NULL)',
       fn_get_product_price('8934008', '2000-01-01'),
       NULL,
       IF(fn_get_product_price('8934008', '2000-01-01') IS NULL, 'PASS', 'FAIL')
UNION ALL
SELECT 'Barcode khong ton tai (ky vong NULL)',
       fn_get_product_price('9999999', CURDATE()),
       NULL,
       IF(fn_get_product_price('9999999', CURDATE()) IS NULL, 'PASS', 'FAIL')
UNION ALL
SELECT 'Tham so NULL (ky vong NULL)',
       fn_get_product_price(NULL, CURDATE()),
       NULL,
       IF(fn_get_product_price(NULL, CURDATE()) IS NULL, 'PASS', 'FAIL');
```

Kết quả thật khi chạy trên database `retail_billing` của dự án:

```text
+-------------------------------------------+-----------+-----------+---------+
| Ca_test                                   | Thuc_te   | Mong_doi  | Ket_qua |
+-------------------------------------------+-----------+-----------+---------+
| Gia hom nay cua 8934008                   | 158000.00 | 158000.00 | PASS    |
| Gia 400 ngay truoc                        | 130000.00 | 130000.00 | PASS    |
| Truoc ngay co gia dau tien (ky vong NULL) |      NULL |      NULL | PASS    |
| Barcode khong ton tai (ky vong NULL)      |      NULL |      NULL | PASS    |
| Tham so NULL (ky vong NULL)               |      NULL |      NULL | PASS    |
+-------------------------------------------+-----------+-----------+---------+
```

Ba điều khiến kỹ thuật này mạnh:

1. **Mắt bạn chỉ cần quét cột `Ket_qua`.** Thấy một chữ `FAIL` là biết ngay ca nào hỏng, không phải so số bằng tay.
2. **Chạy lại tốn 0,01 giây.** Mỗi lần sửa routine, bấm `Ctrl+Enter` lên bảng test là xong.
3. **Nó sống lâu hơn trí nhớ của bạn.** Lưu bảng test này thành file `sql/tests/test_fn_get_product_price.sql`; ba tháng sau có người sửa routine, chạy lại là biết ngay có làm hỏng gì không.

> ⚠️ **Cái bẫy `= NULL`:** trong SQL, `NULL = NULL` không trả về `TRUE` mà trả về `NULL`, nên `IF(x = NULL, 'PASS','FAIL')` **luôn luôn cho FAIL**. Ca kỳ vọng NULL bắt buộc viết `IF(x IS NULL, ...)`. Đây là lỗi mà mọi người đều dính ít nhất một lần.
>
> **So sánh tiền tệ:** `DECIMAL` so sánh bằng `=` là chính xác tuyệt đối, khác hẳn `FLOAT`/`DOUBLE`. Đó là lý do dự án dùng `DECIMAL(12,2)` ở mọi cột tiền — xem [`05-SQL-JAVA-CONVENTION.md`](05-SQL-JAVA-CONVENTION.md) mục 3.

**Các ca test bắt buộc nghĩ tới cho mọi routine:**

| Nhóm | Ca phải test | Vì sao |
|---|---|---|
| Dữ liệu thường | 1 ca có thật, biết trước đáp số | Chứng minh logic đúng |
| Rỗng | Mã không tồn tại, hóa đơn không có dòng nào | Trả NULL hay bảng rỗng — phải khớp hợp đồng |
| NULL | Tham số bị NULL | Java có thể `setNull(...)` — routine không được sập |
| Ngày biên | `Valid_From_Date` đúng bằng `p_date`; khuyến mãi hết hạn hôm nay | Dấu `<` hay `<=` cho ra hai kết quả khác nhau |
| Nhiều bản ghi | Sản phẩm có 2 mốc giá, hóa đơn có nhiều đợt thanh toán | `LIMIT 1` lấy đúng bản ghi mới nhất chưa |
| Số lẻ | Số lượng `1.345` kg | Kiểm tra làm tròn, xem [bẫy số 8](#8-13-cái-bẫy-đặc-thù-của-workbench-và-routine-mysql) |
| Trùng lặp | JOIN làm nhân đôi dòng | `COUNT(*)` trước và sau khi JOIN phải hợp lý |

### 5.3. Test thủ tục trả về bảng dữ liệu

Với `sp_`, ngoài **giá trị** còn phải kiểm ba thứ mà Java phụ thuộc:

```sql
CALL sp_get_invoice_lines('INV-0001');
```

| Kiểm cái gì | Nhìn ở đâu | Vì sao chết người |
|---|---|---|
| **Tên cột (alias)** | Header của Result Grid | Java đọc `rs.getBigDecimal("Thanh_tien")` theo **tên**. Đổi alias = `SQLException: Column not found` |
| **Số dòng** | Action Output: `3 row(s) returned` | Thiếu dòng nghĩa là JOIN sai; thừa dòng nghĩa là JOIN nhân bản |
| **Thứ tự** | Nhìn cột đầu | Không có `ORDER BY` thì MySQL **không cam kết** thứ tự; JTable sẽ hiện lộn xộn |

Đối chiếu header Result Grid với bảng tra ở [`05-SQL-JAVA-CONVENTION.md`](05-SQL-JAVA-CONVENTION.md) mục 6. Muốn xem lại mã nguồn thủ tục đang nằm trên server:

```sql
SHOW CREATE PROCEDURE sp_get_invoice_lines;

-- Hoặc liệt kê toàn bộ routine kèm ngày sửa gần nhất
SELECT ROUTINE_NAME, ROUTINE_TYPE, LAST_ALTERED
FROM   information_schema.routines
WHERE  routine_schema = 'retail_billing'
ORDER  BY LAST_ALTERED DESC;
```

> Cột `LAST_ALTERED` rất hữu ích: nếu nó mới hơn ngày sửa file `sql/04_routines.sql`, tức là **có người sửa thẳng trên server mà quên chép về file** — xem [bẫy số 12](#8-13-cái-bẫy-đặc-thù-của-workbench-và-routine-mysql).

Một cảnh báo riêng cho JDBC: **mỗi `sp_` chỉ nên trả về đúng một `ResultSet`.** Nếu trong thân thủ tục bạn để lọt một câu `SELECT` phụ (ví dụ `SELECT 'Da xong' AS thong_bao;` để debug), MySQL sẽ trả về **hai** bộ kết quả, và Java sẽ đọc nhầm bộ đầu tiên rồi hiện ra một bảng vô nghĩa. Xóa sạch các `SELECT` debug trước khi bàn giao.

### 5.4. Test thủ tục ghi dữ liệu mà không làm bẩn database

Nỗi sợ lớn nhất khi test `INSERT`/`UPDATE`/`DELETE`: **làm hỏng dữ liệu thật**. Cách của dân chuyên nghiệp là bọc trong transaction rồi `ROLLBACK`:

```sql
SELECT COUNT(*) AS so_hd_truoc FROM Invoice;      -- chụp ảnh trước

START TRANSACTION;

CALL sp_create_invoice_header('ZZ-TEST-01', CURDATE(), CURTIME(), 'C01', 'E01', NULL);
CALL sp_add_invoice_line('ZZ-TEST-01', 1, '8934008', 2.000, 158000.00, 0.00);

-- Đọc lại NGAY TRONG transaction để xác nhận đã ghi được
SELECT Invoice_ID, Status FROM Invoice WHERE Invoice_ID = 'ZZ-TEST-01';
SELECT SUM(Quantity * Unit_Price - Discount) AS tong_trong_transaction
FROM   Invoice_Line WHERE Invoice_ID = 'ZZ-TEST-01';

ROLLBACK;                                          -- xóa sạch mọi dấu vết

SELECT COUNT(*) AS so_hd_sau FROM Invoice;         -- phải bằng so_hd_truoc
SELECT COUNT(*) AS con_sot_lai FROM Invoice WHERE Invoice_ID = 'ZZ-TEST-01';  -- phải = 0
```

Kết quả thật khi chạy trên database của dự án:

```text
so_hd_truoc = 17
Invoice_ID = ZZ-TEST-01, Status = PAID        <- ghi được trong transaction
tong_trong_transaction = 316000.00000
so_hd_sau   = 17                              <- ROLLBACK đã dọn sạch
con_sot_lai = 0
```

Bạn có thể chạy đi chạy lại 1.000 lần mà database không dính một dòng rác nào.

**Bốn điều bắt buộc nhớ:**

1. **Bôi đen cả khối rồi `Ctrl + Shift + Enter`.** Chạy từng dòng `Ctrl+Enter` vẫn được vì transaction sống theo phiên kết nối, nhưng chỉ cần bạn đi pha cà phê và quên bấm `ROLLBACK` là hàng loạt dòng khác bị khóa chờ.
2. **Kiểm tra nút Toggle Autocommit.** Workbench mặc định bật autocommit; khi đó `START TRANSACTION` vẫn hoạt động đúng (nó tạm đình chỉ autocommit cho tới `COMMIT`/`ROLLBACK`), nhưng nếu bạn quên gõ `START TRANSACTION` thì `CALL` sẽ ghi thẳng vào database vĩnh viễn.
3. **DDL không rollback được.** `CREATE`/`DROP`/`ALTER TABLE` gây commit ngầm. Transaction chỉ cứu được `INSERT`/`UPDATE`/`DELETE`.
4. **Dùng tiền tố nhận dạng cho dữ liệu test** (`ZZ-TEST-...`). Lỡ có commit nhầm thì cũng dọn được trong 5 giây:

```sql
DELETE FROM Invoice_Line WHERE Invoice_ID LIKE 'ZZ-TEST-%';
DELETE FROM Invoice      WHERE Invoice_ID LIKE 'ZZ-TEST-%';
```

> **Lối thoát hiểm cuối cùng:** database này là dữ liệu học tập, nạp lại sạch bằng một lệnh — `db-setup.ps1`. Đừng ngại thử nghiệm, chỉ nhớ là nó `DROP DATABASE retail_billing` trước khi tạo lại.

### 5.5. Test ca lỗi và cách báo lỗi tử tế cho Java

Routine không chỉ phải đúng khi dữ liệu đẹp, nó còn phải **hỏng cho tử tế** khi dữ liệu xấu. Thử ép nó hỏng:

```sql
START TRANSACTION;
-- Ca 1: Quầy 'C99' không tồn tại -> kỳ vọng lỗi khóa ngoại 1452
CALL sp_create_invoice_header('ZZ-TEST-02', CURDATE(), CURTIME(), 'C99', 'E01', NULL);
ROLLBACK;

START TRANSACTION;
-- Ca 2: Trùng mã hóa đơn -> kỳ vọng lỗi trùng khóa chính 1062
CALL sp_create_invoice_header('INV-0001', CURDATE(), CURTIME(), 'C01', 'E01', NULL);
ROLLBACK;
```

Đây là thông báo thật mà hai ca trên trả về:

```text
Ca 1: ERROR 1452 (23000): Cannot add or update a child row: a foreign key constraint fails
      (`retail_billing`.`invoice`, CONSTRAINT `fk_invoice_counter`
       FOREIGN KEY (`Counter_ID`) REFERENCES `counter` (`Counter_ID`))

Ca 2: ERROR 1062 (23000): Duplicate entry 'INV-0001' for key 'invoice.PRIMARY'
```

Tin tốt: database **đã chặn được** dữ liệu rác, đúng như thiết kế khóa ngoại và khóa chính cam kết. Tin xấu: hãy đọc lại thông báo đó và tự hỏi *"nếu nó hiện lên màn hình cho thu ngân, họ có hiểu không?"*.

Muốn báo lỗi bằng tiếng người, dùng `SIGNAL` để tự ném lỗi nghiệp vụ:

```sql
DELIMITER $$
CREATE PROCEDURE sp_vi_du_kiem_tra(IN p_invoice_id VARCHAR(30))
BEGIN
    IF NOT EXISTS (SELECT 1 FROM Invoice WHERE Invoice_ID = p_invoice_id) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Hoa don khong ton tai';
    END IF;
    -- ... phần xử lý chính
END$$
DELIMITER ;
```

`SQLSTATE '45000'` là mã quy ước cho "lỗi do người dùng định nghĩa". Phía Java, `MESSAGE_TEXT` rơi vào `SQLException.getMessage()` và hiện thẳng lên hộp thoại — thu ngân đọc được ngay. Trong dự án, `InvoiceDao` bắt `SQLException` rồi ném lại thành `RuntimeException` để tầng giao diện hiển thị.

> **Quyết định ranh giới:** kiểm tra hợp lệ nên nằm ở SQL hay Java? Nguyên tắc thực dụng: **ràng buộc dữ liệu** (khóa ngoại, không âm, không trùng) phải do database giữ vì đó là tuyến phòng thủ cuối cùng; **thông báo thân thiện** thì để Java lo. `SIGNAL` dùng cho các quy tắc nghiệp vụ mà khóa ngoại không diễn đạt nổi.

### 5.6. Test đối chiếu chéo

Kỹ thuật trả lời câu hỏi đáng sợ nhất: *"Câu SQL cho ra số, nhưng số đó có ĐÚNG không?"*. Nguyên tắc: **tính lại bằng con đường khác, hai kết quả phải khớp.**

```sql
-- Đường 1: dùng routine báo cáo
CALL sp_report_daily_takings();

-- Đường 2: tính tay thô sơ cho MỘT ngày, không JOIN vòng vèo
SELECT SUM(l.Quantity * l.Unit_Price - l.Discount) AS doanh_thu_tinh_tay
FROM   Invoice i
JOIN   Invoice_Line l ON l.Invoice_ID = i.Invoice_ID
WHERE  i.`Date` = CURDATE();
```

Hai con số của cùng một ngày phải bằng nhau. Lệch nhau là dấu hiệu của một trong ba bệnh kinh điển:

| Triệu chứng | Bệnh | Cách xác nhận |
|---|---|---|
| Báo cáo **lớn hơn** tính tay | JOIN nhân bản dòng (một hóa đơn có nhiều `Payment` làm nhân đôi `Invoice_Line`) | `SELECT COUNT(*)` trước và sau khi thêm bảng vào JOIN |
| Báo cáo **nhỏ hơn** tính tay | Dùng `JOIN` ở chỗ đáng lẽ phải `LEFT JOIN` (mất hóa đơn khách vãng lai không có `Customer_ID`) | Đếm số dòng bị loại: `WHERE Customer_ID IS NULL` |
| Lệch vài đồng lẻ | Làm tròn ở các bước khác nhau | So `ROUND(SUM(...))` với `SUM(ROUND(...))` |

File [`sql/03_queries.sql`](../sql/03_queries.sql) chứa các câu truy vấn bài tập viết theo lối "trần" — dùng chính nó làm **nguồn đối chiếu** cho các `sp_report_*`.

### 5.7. Test hiệu năng bằng EXPLAIN

Câu SQL chạy nhanh trên 17 hóa đơn mẫu không có nghĩa nó sống nổi với 170.000 hóa đơn. `EXPLAIN` cho bạn xem MySQL **định** làm gì trước khi nó làm:

```sql
EXPLAIN
SELECT Price FROM Price_History
WHERE  Barcode = '8934008' AND Valid_From_Date <= CURDATE()
ORDER  BY Valid_From_Date DESC LIMIT 1;
```

Kết quả thật trên dự án:

```text
+----+-------+---------------+-------+---------------+---------+------+----------+----------------------------------+
| id | type  | table         | ...   | possible_keys | key     | rows | filtered | Extra                            |
+----+-------+---------------+-------+---------------+---------+------+----------+----------------------------------+
|  1 | range | Price_History | ...   | PRIMARY       | PRIMARY |    2 |   100.00 | Using where; Backward index scan |
+----+-------+---------------+-------+---------------+---------+------+----------+----------------------------------+
```

Cách đọc nhanh ba cột quan trọng:

| Cột | Giá trị tốt | Giá trị đáng lo |
|---|---|---|
| `type` | `const`, `eq_ref`, `ref`, `range` | **`ALL`** = quét toàn bộ bảng |
| `key` | Tên một index (ở đây là `PRIMARY`) | `NULL` = không dùng index nào |
| `rows` | Số nhỏ (ở đây chỉ 2 dòng) | Số xấp xỉ tổng số dòng của bảng |

Câu trên đạt `type = range`, dùng `PRIMARY` và chỉ chạm 2 dòng — vì khóa chính của `Price_History` là cặp `(Barcode, Valid_From_Date)`, đúng thứ tự mà câu truy vấn cần. `Backward index scan` nghĩa là MySQL đi ngược index để lấy ngày mới nhất, khỏi phải sắp xếp. Đây là ví dụ hoàn hảo của **thiết kế khóa phục vụ đúng truy vấn**.

Xem index hiện có của một bảng:

```sql
SHOW INDEX FROM Price_History;
```

Từ MySQL 8.0.18 trở lên còn có `EXPLAIN ANALYZE` — chạy thật và báo thời gian từng bước (dự án này dùng 8.0.45 nên có sẵn):

```sql
EXPLAIN ANALYZE SELECT ... ;
```

> **Đừng vội thêm index.** Mỗi index làm `INSERT` chậm đi và tốn đĩa. Chỉ thêm khi `EXPLAIN` chỉ ra `type = ALL` trên một bảng lớn, và sau khi thêm phải chạy lại `EXPLAIN` để chứng minh nó thật sự được dùng.

---

## 6. Ráp routine vào source code theo 6 bước

Routine chạy đúng trong Workbench **mới chỉ tồn tại trên server của bạn**. Nó chưa nằm trong Git, đồng đội chưa có, và sẽ bay mất khi ai đó chạy `db-setup`. Sáu bước dưới đây biến nó thành một phần của dự án.

### Bước 1: Chép vào `sql/04_routines.sql` đúng ba chỗ

File này có cấu trúc cố định, routine mới phải xuất hiện ở **hai vị trí**:

```sql
-- CHỖ 1: khối DROP ở đầu file (để chạy lại file nhiều lần không lỗi trùng tên)
DROP FUNCTION IF EXISTS fn_invoice_total;

-- ... (các DROP khác)

DELIMITER $$

-- CHỖ 2: thân routine, đặt vào đúng PHẦN theo chủ đề
--   PHẦN 1: hàm (fn_)        PHẦN 2: tra cứu & sản phẩm
--   PHẦN 3: hóa đơn          PHẦN 4: báo cáo
--   PHẦN 5: ghi dữ liệu, xác thực, sửa/xóa
CREATE FUNCTION fn_invoice_total(...)
...
END$$

DELIMITER ;
```

**Chỗ thứ ba** nằm ở tài liệu: thêm một dòng vào bảng tra 26 routine trong [`05-SQL-JAVA-CONVENTION.md`](05-SQL-JAVA-CONVENTION.md) mục 6. Bỏ qua bước này thì Dev Java không biết routine của bạn tồn tại.

> **Vì sao phải có `DROP ... IF EXISTS`?** Để file **chạy lại được nhiều lần** (idempotent). Không có nó, lần chạy thứ hai sẽ dừng ở `Error 1304: FUNCTION already exists` và các routine phía sau không được tạo — một lỗi rất khó nhận ra vì file có vẻ như "đã chạy rồi".

### Bước 2: Chạy lại **cả file** để chứng minh nó tự đứng được

Đây là bước hay bị bỏ qua nhất, và cũng là bước hay lộ lỗi nhất. Routine của bạn chạy được trong Workbench **không** đảm bảo file chạy được từ đầu đến cuối.

```bash
powershell -ExecutionPolicy Bypass -File .\scripts\ps1\sql-create-db.ps1
```

Lệnh này chạy `01_schema.sql` rồi `04_routines.sql` — tức là dựng lại bảng trống và nạp toàn bộ routine. Sau đó nạp lại dữ liệu mẫu:

```bash
powershell -ExecutionPolicy Bypass -File .\scripts\ps1\sql-seed-data.ps1
```

Hoặc làm cả ba bước bằng một lệnh (`01` + `02` + `04`):

```bash
powershell -ExecutionPolicy Bypass -File .\scripts\ps1\db-setup.ps1
```

Trong Workbench thì dùng **File → Open SQL Script** (`Ctrl+Shift+O`), mở `sql/04_routines.sql`, rồi `Ctrl+Shift+Enter`. Chạy xong, kiểm chứng lại số lượng:

```sql
SELECT ROUTINE_TYPE, COUNT(*) FROM information_schema.routines
WHERE routine_schema = 'retail_billing' GROUP BY ROUTINE_TYPE;
```

Rồi chạy lại bảng ca test ở [5.2](#52-bảng-ca-test-pass-fail-bằng-union-all) lần nữa — lần này trên database vừa dựng lại từ con số 0.

### Bước 3: Cập nhật hợp đồng cho Dev Java

Gửi cho Dev Java (hoặc ghi vào commit message) đúng 4 thông tin:

```
Routine  : fn_invoice_total
Gọi kiểu : {? = call fn_invoice_total(?)}
Vào      : p_invoice_id VARCHAR(30)
Ra       : DECIMAL(12,2), trả NULL nếu hóa đơn không tồn tại
```

### Bước 4: Java gọi routine — ba khuôn mẫu

**Khuôn mẫu A — Hàm trả giá trị đơn (`fn_`).** Cú pháp `{? = call ...}`, tham số 1 luôn là giá trị trả về, tham số đầu vào bắt đầu từ 2. Mẫu có thật trong [`ProductDao.priceOn()`](../src/billing/dao/ProductDao.java):

```java
public BigDecimal priceOn(String barcode, LocalDate date) {
    String sql = "{? = call fn_get_product_price(?, ?)}";

    try (Connection c = Db.getConnection();
         CallableStatement cs = c.prepareCall(sql)) {

        cs.registerOutParameter(1, Types.DECIMAL);   // 1 = giá trị hàm trả về
        cs.setString(2, barcode);                    // 2, 3 = tham số đầu vào
        cs.setDate(3, Date.valueOf(date));
        cs.execute();

        return cs.getBigDecimal(1);                  // null = chưa có giá trước ngày đó
    } catch (SQLException e) {
        throw new RuntimeException("Khong tim duoc gia: " + e.getMessage(), e);
    }
}
```

**Khuôn mẫu B — Thủ tục trả bảng (`sp_`).** Nếu kết quả chỉ để đổ lên `JTable`, dùng luôn tiện ích có sẵn, như [`ReportDao`](../src/billing/dao/ReportDao.java) đang làm:

```java
public QueryResult dailyTakings() {
    return QueryResult.call("{call sp_report_daily_takings()}");
}
```

`QueryResult` tự đọc tên cột từ `ResultSetMetaData`, nên **alias bạn đặt trong SQL chính là tiêu đề cột trên giao diện**. Nếu cần ánh xạ sang Model Java thì đọc theo tên cột:

```java
try (ResultSet rs = cs.executeQuery()) {
    while (rs.next()) {
        int    dong      = rs.getInt("Dong");
        String sanPham   = rs.getString("San_pham");
        BigDecimal tien  = rs.getBigDecimal("Thanh_tien");
    }
}
```

**Khuôn mẫu C — Thủ tục ghi dữ liệu, nằm trong transaction của Java.** Ranh giới transaction do Java giữ, các `sp_` được gọi tuần tự **trên cùng một `Connection`** — xem [`InvoiceDao.createInvoice()`](../src/billing/dao/InvoiceDao.java):

```java
conn = Db.getConnection();
conn.setAutoCommit(false);                 // ==== MỞ TRANSACTION ====
// gọi sp_create_invoice_header, sp_add_invoice_line (batch), sp_add_payment
conn.commit();                             // ==== CHỐT ====
// nếu có lỗi: conn.rollback();
```

Vì vậy **`sp_` ghi dữ liệu không được tự `COMMIT` bên trong**. Nếu bạn nhét `COMMIT;` vào thân thủ tục, Java sẽ mất khả năng rollback và một lỗi ở dòng hàng thứ 5 sẽ để lại hóa đơn nửa vời trong database.

### Bước 5: Test lại từ phía Java

Đây là lúc kiểm tra **cầu nối JDBC**, thứ mà Workbench không kiểm được: kiểu dữ liệu có khớp không, alias có đọc ra không, `NULL` có về đúng `null` không. Dự án có sẵn [`src/billing/test/QuickTestRunner.java`](../src/billing/test/QuickTestRunner.java) — thêm một hàm kiểm thử vào đó:

```java
private static void testInvoiceTotal() {
    InvoiceDao dao = new InvoiceDao();
    BigDecimal tong = dao.invoiceTotal("INV-0001");
    assertCheck("fn_invoice_total: tong hoa don INV-0001",
            tong != null && tong.compareTo(new BigDecimal("135100.00")) == 0,
            "Mong doi 135100.00, thuc te: " + tong);

    assertCheck("fn_invoice_total: hoa don khong ton tai tra null",
            dao.invoiceTotal("INV-9999") == null,
            "Mong doi null");
}
```

Chạy nó bằng `javac`/`java` thuần, không cần bật giao diện Swing. Hai lệnh PowerShell sau chạy từ thư mục gốc dự án (biên dịch xong khoảng 3 giây):

```bash
javac -encoding UTF-8 -d out -cp "lib\*" (Get-ChildItem -Recurse -Filter *.java src | ForEach-Object FullName)
```

```bash
java -cp "out;lib\*" billing.test.QuickTestRunner
```

> Phải chạy từ **thư mục gốc dự án**, vì [`Db.java`](../src/billing/db/Db.java) đọc `config.properties` theo đường dẫn tương đối với thư mục hiện hành. Chạy từ chỗ khác sẽ nhận thông báo `[Db] Khong doc duoc config.properties, dung gia tri mac dinh.` và kết nối bằng mật khẩu rỗng.

> ⚠️ **Một lỗi đang tồn tại trong `QuickTestRunner`, rất đáng làm ví dụ:** ca test DAO trong file đó đang tra mã vạch `8934567890123` (13 số), trong khi `02_seed.sql` chỉ có mã 7 số (`8934001` … `8934010`). Kết quả là ca "lấy giá thịt ba chỉ" luôn báo ❌ dù `fn_get_product_price` hoàn toàn đúng. Bài học: **kỳ vọng trong test phải lấy từ dữ liệu seed thật**, và khi test đỏ thì nghi ngờ bài test trước khi nghi ngờ routine.

### Bước 6: Xác nhận bằng tab Nhật ký SQL của app

Bật app (`run.ps1` hoặc tab **Java** của SetupApp), thao tác trên giao diện rồi mở tab **Nhat ky SQL**. Mọi câu lệnh JDBC gửi xuống MySQL đều được [`SqlLog`](../src/billing/db/SqlLog.java) ghi lại kèm tham số, thời gian và số dòng:

```text
[14:22:07]  12 ms, 1 dong
{? = call fn_get_product_price(?, ?)}
   tham so (?): 1=8934008, 2=2026-09-24
```

Đây là bằng chứng cuối cùng rằng routine của bạn được gọi **đúng tên, đúng tham số, đúng thứ tự**.

### Checklist bàn giao

- [ ] Routine có trong khối `DROP ... IF EXISTS` ở đầu `04_routines.sql`
- [ ] Thân routine nằm trong khối `DELIMITER $$ ... DELIMITER ;`, đúng PHẦN chủ đề
- [ ] Tên theo quy ước: `fn_` / `sp_`, tham số `p_`, biến nội bộ `v_`
- [ ] Alias cột đặt xong là **cố định**, đã ghi vào tài liệu
- [ ] Chạy lại cả file `04_routines.sql` từ database trống: không lỗi
- [ ] Bảng ca test PASS toàn bộ trên database vừa dựng lại
- [ ] `EXPLAIN` không có `type = ALL` trên bảng lớn
- [ ] Thủ tục ghi dữ liệu **không** tự `COMMIT`
- [ ] Không còn `SELECT` debug nào lọt trong thân `sp_`
- [ ] Đã thêm dòng vào bảng tra routine ở `docs/05-SQL-JAVA-CONVENTION.md`
- [ ] Đã báo Dev Java: tên routine, cú pháp gọi, tham số, kiểu trả về

---

## 7. Bài thực hành trọn vẹn viết fn_invoice_total

Đi hết một vòng I-P-O-T cho một routine mới: **tính tổng tiền của một hóa đơn**. Công thức `Quantity * Unit_Price - Discount` đang bị chép lại ở **6 thủ tục** khác nhau trong `04_routines.sql` (`sp_get_recent_invoices`, `sp_get_invoice_header`, `sp_get_invoice_lines`, `sp_get_invoice_returns`, `sp_report_daily_takings`, `sp_report_big_returns`), nên gom thành một hàm là việc đáng làm.

> Toàn bộ SQL dưới đây **đã được chạy thật** trên database `retail_billing` của dự án; các con số là kết quả thật. Nếu bạn nạp lại seed mà số hóa đơn thay đổi thì con số sẽ khác, nhưng cách làm thì không đổi.

### Bước 1: Hợp đồng I-P-O-T

| Mục | Nội dung |
|---|---|
| **Tên / Loại** | `fn_invoice_total` — FUNCTION, `RETURNS DECIMAL(12,2)` |
| **INPUT** | `p_invoice_id VARCHAR(30)` — đúng kiểu cột `Invoice.Invoice_ID` |
| **PROCESSING** | `SUM(Quantity * Unit_Price - Discount)` trên `Invoice_Line`, lọc theo `Invoice_ID` |
| **OUTPUT** | Một số tiền. **Hóa đơn không tồn tại → `NULL`** (để Java phân biệt được với hóa đơn có tổng bằng 0) |
| **TEST** | 1) `INV-0001` = 135100.00 · 2) `INV-9999` → NULL · 3) tham số NULL → NULL · 4) đối chiếu chéo với cách tính tay cho 6 hóa đơn |
| **AI GỌI** | `InvoiceDao.invoiceTotal()` (sẽ viết thêm ở bước 7) |

### Bước 2: Khám phá dữ liệu thật

```sql
SELECT Invoice_ID, SUM(Quantity * Unit_Price - Discount) AS Tong
FROM   Invoice_Line
GROUP  BY Invoice_ID
ORDER  BY Invoice_ID
LIMIT  5;
```

```text
+------------+--------------+
| Invoice_ID | Tong         |
+------------+--------------+
| INV-0001   | 135100.00000 |
| INV-0002   | 307000.00000 |
| INV-0003   | 322250.00000 |
| INV-0004   | 128000.00000 |
| INV-0005   | 377800.00000 |
+------------+--------------+
```

### Bước 3: Phát hiện cái bẫy ngay từ dữ liệu

Nhìn kỹ: `135100.00000` có **5 chữ số thập phân**, không phải 2. Vì sao? Quy tắc nhân `DECIMAL` của MySQL cộng dồn phần thập phân:

```
Quantity DECIMAL(10,3)  ×  Unit_Price DECIMAL(12,2)  =  kết quả có 3 + 2 = 5 chữ số thập phân
```

Nếu để nguyên, hàm khai báo `RETURNS DECIMAL(12,2)` sẽ **tự cắt** về 2 chữ số theo quy tắc của MySQL, và với hàng bán theo cân (`1.345 kg`) bạn có thể lệch vài đồng so với cách tính của kế toán. Tự kiểm soát bằng `ROUND(..., 2)` thay vì phó mặc:

```sql
SELECT ROUND(SUM(Quantity * Unit_Price - Discount), 2) AS Tong
FROM   Invoice_Line WHERE Invoice_ID = 'INV-0001';     -- 135100.00
```

**Đây chính là giá trị của bước khám phá dữ liệu:** cái bẫy này lộ ra ở giây thứ 30, thay vì lộ ra sau hai tuần khi kế toán phát hiện báo cáo lệch.

### Bước 4: Bọc thành routine

```sql
DROP FUNCTION IF EXISTS fn_invoice_total;

DELIMITER $$

CREATE FUNCTION fn_invoice_total(
    p_invoice_id VARCHAR(30)
)
RETURNS DECIMAL(12,2)
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE v_total DECIMAL(12,2) DEFAULT NULL;

    SELECT ROUND(SUM(l.Quantity * l.Unit_Price - l.Discount), 2)
    INTO   v_total
    FROM   Invoice_Line l
    WHERE  l.Invoice_ID = p_invoice_id;

    RETURN v_total;
END$$

DELIMITER ;
```

Ba chi tiết không được bỏ:

- **`DEFAULT NULL` trong `DECLARE`.** Khi `SELECT ... INTO` không tìm thấy dòng nào, biến **giữ nguyên giá trị cũ**. Không đặt giá trị mặc định là bạn đang phụ thuộc vào may rủi.
- **`DETERMINISTIC` + `READS SQL DATA`.** Không khai báo, trên server có bật binary log bạn sẽ gặp `Error 1418: This function has none of DETERMINISTIC, NO SQL, or READS SQL DATA...`.
- **Khối này bắt buộc chạy bằng `Ctrl + Shift + Enter`** (chạy cả khối), không phải `Ctrl + Enter`.

### Bước 5: Bảng ca test

```sql
SELECT 'HD co that INV-0001' AS Ca_test,
       fn_invoice_total('INV-0001') AS Thuc_te,
       135100.00                    AS Mong_doi,
       IF(fn_invoice_total('INV-0001') = 135100.00, 'PASS', 'FAIL') AS Ket_qua
UNION ALL
SELECT 'HD khong ton tai', fn_invoice_total('INV-9999'), NULL,
       IF(fn_invoice_total('INV-9999') IS NULL, 'PASS', 'FAIL')
UNION ALL
SELECT 'Tham so NULL', fn_invoice_total(NULL), NULL,
       IF(fn_invoice_total(NULL) IS NULL, 'PASS', 'FAIL');
```

```text
+---------------------+-----------+-----------+---------+
| Ca_test             | Thuc_te   | Mong_doi  | Ket_qua |
+---------------------+-----------+-----------+---------+
| HD co that INV-0001 | 135100.00 | 135100.00 | PASS    |
| HD khong ton tai    |      NULL |      NULL | PASS    |
| Tham so NULL        |      NULL |      NULL | PASS    |
+---------------------+-----------+-----------+---------+
```

### Bước 6: Đối chiếu chéo trên nhiều hóa đơn

Ba ca test chưa đủ — hãy bắt hàm tự đối chiếu với cách tính thủ công trên **mọi** hóa đơn:

```sql
SELECT i.Invoice_ID,
       fn_invoice_total(i.Invoice_ID) AS Ham_moi,
       (SELECT ROUND(SUM(l.Quantity * l.Unit_Price - l.Discount), 2)
          FROM Invoice_Line l WHERE l.Invoice_ID = i.Invoice_ID) AS Tinh_tay,
       IF(fn_invoice_total(i.Invoice_ID) <=>
          (SELECT ROUND(SUM(l.Quantity * l.Unit_Price - l.Discount), 2)
             FROM Invoice_Line l WHERE l.Invoice_ID = i.Invoice_ID),
          'PASS', 'FAIL') AS Ket_qua
FROM   Invoice i
ORDER  BY i.Invoice_ID;
```

```text
+------------+-----------+-----------+---------+
| Invoice_ID | Ham_moi   | Tinh_tay  | Ket_qua |
+------------+-----------+-----------+---------+
| INV-0001   | 135100.00 | 135100.00 | PASS    |
| INV-0002   | 307000.00 | 307000.00 | PASS    |
| INV-0003   | 322250.00 | 322250.00 | PASS    |
| INV-0004   | 128000.00 | 128000.00 | PASS    |
| INV-0005   | 377800.00 | 377800.00 | PASS    |
| INV-0006   |  95000.00 |  95000.00 | PASS    |
+------------+-----------+-----------+---------+
```

> 💡 Để ý toán tử `<=>` (null-safe equal): nó coi `NULL <=> NULL` là `TRUE`. Nhờ vậy một câu so sánh duy nhất xử lý được cả hóa đơn có dòng hàng lẫn hóa đơn rỗng, khỏi phải tách hai ca.

### Bước 7: Ráp vào source code

**7a.** Thêm `DROP FUNCTION IF EXISTS fn_invoice_total;` vào khối DROP đầu `sql/04_routines.sql`, và đặt thân hàm vào **PHẦN 1: HÀM SQL**, ngay sau `fn_get_product_price`.

**7b.** Chạy lại cả file, rồi chạy lại bảng ca test:

```bash
powershell -ExecutionPolicy Bypass -File .\scripts\ps1\db-setup.ps1
```

**7c.** Thêm phương thức vào `InvoiceDao` (dán theo khuôn mẫu A ở mục 6):

```java
/** Tổng tiền của một hóa đơn. Gọi Stored Function fn_invoice_total(p_invoice_id). */
public BigDecimal invoiceTotal(String invoiceId) {
    String sql = "{? = call fn_invoice_total(?)}";
    long t0 = System.currentTimeMillis();
    BigDecimal total = null;

    try (Connection c = Db.getConnection();
         CallableStatement cs = c.prepareCall(sql)) {

        cs.registerOutParameter(1, Types.DECIMAL);
        cs.setString(2, invoiceId);
        cs.execute();
        total = cs.getBigDecimal(1);

    } catch (SQLException e) {
        throw new RuntimeException("Khong tinh duoc tong hoa don: " + e.getMessage(), e);
    }

    SqlLog.add(sql, new Object[]{invoiceId}, System.currentTimeMillis() - t0,
               total == null ? 0 : 1);
    return total;      // null = hóa đơn không tồn tại
}
```

**7d.** Thêm ca test vào `QuickTestRunner` (mẫu ở [Bước 5 mục 6](#bước-5-test-lại-từ-phía-java)), chạy và xem hai dấu ✅.

**7e.** Thêm dòng này vào bảng tra của `docs/05-SQL-JAVA-CONVENTION.md`:

| Tên Routine | Loại | Tham số | Ý nghĩa nghiệp vụ | Nơi gọi trong Java |
|---|---|---|---|---|
| `fn_invoice_total` | FUNCTION | `p_invoice_id` | Tổng tiền phải trả của một hóa đơn | `InvoiceDao.invoiceTotal()` |

### Bài tập nâng cao

Viết `fn_invoice_net_total(p_invoice_id)` — tổng tiền **sau khi trừ hàng đã trả lại**. Gợi ý I-P-O-T:

- **P:** `LEFT JOIN` bảng `` `Return` `` theo cặp khóa `(Invoice_ID, Line_Number)`; tiền hoàn của mỗi lần trả tính theo tỷ lệ `Return_Quantity / Quantity` như `sp_get_invoice_returns` đang làm.
- **T (ca bắt buộc):** hóa đơn không có lần trả nào (kết quả phải bằng `fn_invoice_total`) · hóa đơn trả một phần · hóa đơn trả hết (kết quả 0, **không được âm**) · một dòng hàng bị trả **nhiều lần** (`Return_ID` khác nhau — rất dễ bị `JOIN` nhân đôi, hãy đếm số dòng trước và sau khi JOIN).
- Nhớ dùng dấu nháy ngược quanh `` `Return` `` vì đó là từ khóa dành riêng của MySQL.

---

## 8. 13 cái bẫy đặc thù của Workbench và routine MySQL

| # | Cái bẫy | Triệu chứng | Cách xử lý |
|---|---|---|---|
| 1 | **`DELIMITER` bị bỏ quên** | `Error 1064` ngay tại dòng `;` đầu tiên trong thân routine | Bọc `DELIMITER $$ ... DELIMITER ;` và chạy cả khối bằng `Ctrl+Shift+Enter` |
| 2 | **Safe Update Mode** | `Error 1175: You are using safe update mode...` khi `UPDATE`/`DELETE` | Thêm điều kiện lọc theo khóa chính; hoặc *Edit → Preferences → SQL Editor* → bỏ chọn **Safe Updates** rồi *Query → Reconnect to Server* |
| 3 | **`Limit to 1000 rows`** | Test đếm ra đúng 1000 dòng "tròn trĩnh" đáng ngờ | Workbench tự chèn `LIMIT 1000`. Khi đếm, luôn dùng `SELECT COUNT(*)` thay vì đếm dòng trên lưới |
| 4 | **Quên `ROLLBACK`** | Dữ liệu test lọt vào database thật; các phiên khác bị treo chờ khóa | Luôn viết `ROLLBACK;` **cùng lúc** với `START TRANSACTION;`, trước khi viết phần giữa |
| 5 | **DDL không rollback được** | `DROP TABLE` trong transaction vẫn mất bảng | `CREATE`/`ALTER`/`DROP` gây commit ngầm. Chỉ `INSERT`/`UPDATE`/`DELETE` mới cứu được |
| 6 | **`SELECT ... INTO` không có dòng nào** | Biến giữ giá trị của lần chạy trước, hàm trả số vô lý | Luôn `DECLARE v_x ... DEFAULT NULL;` |
| 7 | **`= NULL` trong bảng test** | Ca kỳ vọng NULL luôn báo FAIL | Dùng `IS NULL`, hoặc toán tử null-safe `<=>` |
| 8 | **Scale của `DECIMAL` khi nhân** | Tổng tiền ra `135100.00000` (5 số lẻ), lệch vài đồng với kế toán | Chủ động `ROUND(..., 2)`, đừng phó mặc cho MySQL tự cắt |
| 9 | **`ONLY_FULL_GROUP_BY`** | `Error 1055: ... not in GROUP BY clause...` | `sql_mode` của dự án có bật chế độ này. Đưa **mọi** cột không phải hàm tổng hợp vào `GROUP BY` — đúng chuẩn SQL, đừng tắt sql_mode |
| 10 | **Thiếu `DETERMINISTIC`** | `Error 1418` khi `CREATE FUNCTION` | Khai báo `DETERMINISTIC` + `READS SQL DATA` như mọi hàm trong `04_routines.sql` |
| 11 | **Thủ tục trả nhiều `ResultSet`** | Giao diện hiện bảng lạ hoắc, hoặc Java báo lỗi khó hiểu | Xóa hết `SELECT` debug trong thân `sp_`; mỗi thủ tục chỉ trả đúng một bộ kết quả |
| 12 | **Sửa routine trên server mà quên chép về file** | Máy bạn chạy đúng, máy đồng đội chạy sai; chạy `db-setup` là mất trắng công sức | `information_schema.routines.LAST_ALTERED` mới hơn ngày sửa file = dấu hiệu lệch. **Nguồn sự thật luôn là `sql/04_routines.sql` trong Git** |
| 13 | **Đổi alias cột cho "đẹp"** | `SQLException: Column 'San_pham' not found`, hoặc tiêu đề bảng trên app đổi ngẫu nhiên | Alias là **hợp đồng**. Muốn đổi phải báo Dev Java và sửa cả `docs/05` lẫn code DAO |

> **Bẫy thứ 14 dành cho người cẩn thận:** ngày tháng. `CURDATE()` trả ngày theo múi giờ của **server**, trong khi JDBC URL của dự án khai `serverTimezone=Asia/Ho_Chi_Minh`. Nếu một ngày nào đó báo cáo "hôm nay" lệch mất một ngày, hãy bắt đầu điều tra từ `SELECT NOW(), CURDATE(), @@time_zone;`.

---

## 9. Bảng đối chiếu Dev SQL viết một lèo và Dev SQL I-P-O-T

| Tiêu chí | Dev SQL "viết một lèo" | Dev SQL I-P-O-T |
|---|---|---|
| **Bắt đầu từ đâu** | Gõ thẳng `CREATE PROCEDURE` vào file | Viết hợp đồng I-P-O-T 5 dòng, rồi thử `SELECT` trần trong Workbench |
| **Thời gian biết mình sai** | 15–30 phút (sau khi nạp DB và bật app) | 5 giây (`Ctrl+Enter`) |
| **Cách kiểm tra kết quả** | Nhìn bảng trên giao diện, thấy có số là tin | Bảng ca test PASS/FAIL + đối chiếu chéo bằng cách tính khác |
| **Ca biên** | Chỉ ca đẹp; NULL và bảng rỗng để app tự chịu | NULL, rỗng, ngày biên, số lẻ đều có ca test riêng |
| **Test thủ tục ghi** | Chạy thật rồi xóa tay, thỉnh thoảng quên | `START TRANSACTION` → `CALL` → kiểm tra → `ROLLBACK` |
| **Hiệu năng** | "Chạy nhanh mà" (trên 17 dòng dữ liệu) | `EXPLAIN` trước khi bàn giao, không để `type = ALL` |
| **Bàn giao cho Java** | Nhắn "xong rồi nhé" | Tên routine, cú pháp gọi, tham số, kiểu trả về, đã cập nhật bảng tra |
| **Khi cần sửa routine cũ** | Sợ không dám đụng | Chạy lại bảng ca test: PASS = an toàn, FAIL = biết ngay chỗ hỏng |

---

## 10. Checklist dán trước bàn làm việc

**Trước khi gõ dòng SQL đầu tiên**

- [ ] Đã bật MySQL và Workbench nối đúng `127.0.0.1:3306`, schema `retail_billing`
- [ ] Đã chạy `SELECT VERSION(), @@port, DATABASE();` để chắc không nối nhầm server
- [ ] Đã viết hợp đồng I-P-O-T: vào gì, ra gì, alias gì, không có dữ liệu thì trả gì
- [ ] Đã `SELECT * ... LIMIT 10` nhìn dữ liệu thật của các bảng sẽ đụng tới

**Trong lúc viết**

- [ ] Thử từng mệnh đề bằng `Ctrl+Enter`, chưa vội bọc routine
- [ ] Dùng `SET @p_...` làm tham số giả, đặt trùng tên tham số tương lai
- [ ] Mỗi khi thêm một `JOIN`, kiểm `COUNT(*)` xem số dòng có bị nhân lên không
- [ ] Tiền tệ luôn `DECIMAL`, chủ động `ROUND(..., 2)`, tuyệt đối không `FLOAT`/`DOUBLE`

**Trước khi bảo "xong"**

- [ ] Bảng ca test PASS toàn bộ, có đủ ca NULL và ca rỗng
- [ ] Đã đối chiếu chéo với một cách tính khác
- [ ] Đã `EXPLAIN`, không có `type = ALL` trên bảng lớn
- [ ] Thủ tục ghi không tự `COMMIT`; không còn `SELECT` debug
- [ ] Đã chép vào `sql/04_routines.sql` (cả `DROP` lẫn thân) và chạy lại **cả file** từ DB trống
- [ ] Đã thêm dòng vào bảng tra ở `docs/05-SQL-JAVA-CONVENTION.md`
- [ ] Đã test lại từ phía Java (`QuickTestRunner`) và xem tab **Nhat ky SQL** trong app

---

> 💡 **Đọc tiếp:**
> - [`05-SQL-JAVA-CONVENTION.md`](05-SQL-JAVA-CONVENTION.md) — bản hợp đồng: quy tắc đặt tên, ánh xạ kiểu dữ liệu, bảng tra toàn bộ routine. **Tài liệu bạn mở nhiều nhất.**
> - [`10-HUONG-DAN-TEST-TUNG-PHAN-CHO-JAVA-DEV.md`](10-HUONG-DAN-TEST-TUNG-PHAN-CHO-JAVA-DEV.md) — phía bên kia cây cầu: Dev Java test từng phần ra sao.
> - [`02-JDBC.md`](02-JDBC.md) — `CallableStatement`, transaction, `BigDecimal`: hiểu Java nhận kết quả của bạn như thế nào.
> - [`03-SCHEMA.md`](03-SCHEMA.md) và [`01-NORMALIZATION.md`](01-NORMALIZATION.md) — vì sao 16 bảng được chia như hiện tại.

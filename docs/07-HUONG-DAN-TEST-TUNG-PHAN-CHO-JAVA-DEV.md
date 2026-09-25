# Cẩm nang Fast Feedback Loop cho Java Developer: Kỹ năng Test từng phần, thấy kết quả ngay trước mắt

> **Dành cho:** Bất kỳ Java Developer nào từng trải qua cảm giác đau đớn: *Viết code suốt 2 tiếng đồng hồ một mạch từ Database, DAO, Model đến UI, để rồi khi bấm nút Chạy (Run) thì ứng dụng bung ra một đống lỗi đỏ lòm, không biết lỗi bắt đầu từ đâu!*
>
> **Mục tiêu:** Trang bị tư duy và kỹ thuật **"Viết đến đâu — Thấy kết quả đến đó" (Fast Feedback Loop)**. Bạn sẽ biết cách kiểm thử (test) từng phương thức, từng class độc lập chỉ trong **0.5 giây**, phát hiện ngay các lỗi logic (boundary, null, sai số, lỗi query) và sửa ngay lập tức khi code còn đang nóng hổi trong đầu.

---

## MỤC LỤC

1. [Căn bệnh kinh điển: "Viết một lèo rồi cầu nguyện" (The Big Bang Trap)](#1-căn-bệnh-kinh-điển-viết-một-lèo-rồi-cầu-nguyện-the-big-bang-trap)
   - [1.1. Vòng lặp phản hồi (Feedback Loop) là gì?](#11-vòng-lặp-phản-hồi-feedback-loop-là-gì)
   - [1.2. Cái giá đắt đỏ của việc phát hiện lỗi muộn](#12-cái-giá-đắt-đỏ-của-việc-phát-hiện-lỗi-muộn)
2. [Tư duy "Chia để trị": Bóc tách ứng dụng thành 4 tầng để test](#2-tư-duy-chia-để-trị-bóc-tách-ứng-dụng-thành-4-tầng-để-test)
   - [Tầng 1: Thuần Logic & Tính toán (Model, Validator, Helper) — Test trong 0.01s](#tầng-1-thuần-logic--tính-toán-model-validator-helper--test-trong-001s)
   - [Tầng 2: Tầng dữ liệu (DAO / Database Access) — Test độc lập không cần bật UI](#tầng-2-tầng-dữ-liệu-dao--database-access--test-độc-lập-không-cần-bật-ui)
   - [Tầng 3: Tầng nghiệp vụ (Service / Business Rules) — Cô lập bằng Mocking](#tầng-3-tầng-nghiệp-vụ-service--business-rules--cô-lập-bằng-mocking)
   - [Tầng 4: Tầng giao diện (Swing UI / Web Controller) — Giữ UI "ngu ngơ"](#tầng-4-tầng-giao-diện-swing-ui--web-controller--giữ-ui-ngu-ngơ)
3. [5 "Vũ khí" test ngay lập tức trong Java (Từ 0 đồng đến Chuyên nghiệp)](#3-5-vũ-khí-test-ngay-lập-tức-trong-java-từ-0-đồng-đến-chuyên-nghiệp)
   - [Vũ khí 1: JShell (Java REPL) — Thử nghiệm biểu thức trong 1 giây](#vũ-khí-1-jshell-java-repl--thử-nghiệm-biểu-thức-trong-1-giây)
   - [Vũ khí 2: IntelliJ Scratch Files & Shortcuts — Chạy thử code nháp độc lập](#vũ-khí-2-intellij-scratch-files--shortcuts--chạy-thử-code-nháp-độc-lập)
   - [Vũ khí 3: Quick Test Harness (`main` runner riêng biệt) — Siêu tốc, không cần cài đặt](#vũ-khí-3-quick-test-harness-main-runner-riêng-biệt--siêu-tốc-không-cần-cài-đặt)
   - [Vũ khí 4: Tiêu chuẩn vàng JUnit 5 — Bấm nút xanh 🟢 thấy kết quả trong 0.2s](#vũ-khí-4-tiêu-chuẩn-vàng-junit-5--bấm-nút-xanh--thấy-kết-quả-trong-02s)
   - [Vũ khí 5: Giả lập thế giới bên ngoài với Mockito (Mocking)](#vũ-khí-5-giả-lập-thế-giới-bên-ngoài-với-mockito-mocking)
4. [Thực hành chi tiết trên codebase DemoBillingManagement](#4-thực-hành-chi-tiết-trên-codebase-demobillingmanagement)
   - [Thực hành 1: Bắt lỗi logic tính tiền trong `CartLine`](#thực-hành-1-bắt-lỗi-logic-tính-tiền-trong-cartline)
   - [Thực hành 2: Test Stored Function `priceOn()` trong `ProductDao` mà không cần bật UI](#thực-hành-2-test-stored-function-priceon-trong-productdao-mà-không-cần-bật-ui)
   - [Thực hành 3: Test Transaction an toàn với Rollback trong `InvoiceDao`](#thực-hành-3-test-transaction-an-toàn-với-rollback-trong-invoicedao)
5. [Checklist các "Ca biên" (Edge Cases) bắt sạch lỗi logic](#5-checklist-các-ca-biên-edge-cases-bắt-sạch-lỗi-logic)
6. [Tư duy TDD thực dụng (Pragmatic Test-Driven Development)](#6-tư-duy-tdd-thực-dụng-pragmatic-test-driven-development)
7. [Bảng đối chiếu: Dev "Cầu nguyện" vs Dev "Fast Feedback"](#7-bảng-đối-chiếu-dev-cầu-nguyện-vs-dev-fast-feedback)
8. [Tổng kết: 4 thói quen vàng của Java Developer chuyên nghiệp](#8-tổng-kết-4-thói-quen-vàng-của-java-developer-chuyên-nghiệp)

---

## 1. Căn bệnh kinh điển: "Viết một lèo rồi cầu nguyện" (The Big Bang Trap)

Hầu hết các lập trình viên khi mới học Java hoặc làm dự án thường mắc phải quy trình làm việc sau:

```
[Nhận yêu cầu] 
      ↓
[Viết bảng SQL] 
      ↓
[Viết Model Java] 
      ↓
[Viết DAO JDBC] 
      ↓
[Viết giao diện UI / Controller]  <--- 2 ĐẾN 3 TIẾNG TRÔI QUA (Chưa hề chạy thử dòng nào!)
      ↓
[BẤM RUN CHẠY ỨNG DỤNG LẦN ĐẦU TIÊN VÀ CẦU NGUYỆN 🙏]
      ↓
💥 BÙM! Ứng dụng sập! 
- Lỗi NullPointerException ở dòng 84
- Không hiển thị dữ liệu lên bảng
- Tiền tính ra số âm
- Giao diện bị đơ cứng
```

### 1.1. Vòng lặp phản hồi (Feedback Loop) là gì?

> **Feedback Loop (Vòng phản hồi):** Là khoảng thời gian từ lúc bạn **gõ một dòng code** cho đến lúc bạn **biết chắc chắn dòng code đó chạy đúng hay sai**.

```
Quy trình "Cầu nguyện" (Chậm chạp & Đau đớn):
[Gõ code 2 tiếng] ----------------------------------------------> [Test 1 lần] -> [Sập]
└──────────────────── Feedback Loop = 120 phút ─────────────────────┘

Quy trình "Fast Feedback" (Nhanh, Tự tin & Thảnh thơi):
[Gõ 1 hàm 3 phút] -> [Test 0.5s: Xanh ✅] 
[Gõ tiếp 3 phút]  -> [Test 0.5s: Đỏ ❌ -> Sửa 10s -> Xanh ✅]
└────── Feedback Loop = 3 phút ──────┘
```

Khi Feedback Loop là **120 phút**, nếu gặp lỗi, bạn phải lục lọi giữa hàng nghìn dòng code vừa viết, đặt `System.out.println` vô tội vạ khắp nơi, restart lại cả ứng dụng chỉ để thử một giá trị nhỏ.

Khi Feedback Loop là **3 phút (hoặc vài giây)**: Bạn vừa viết xong hàm `lineTotal()`, bạn test ngay giá trị `(sl: 2, gia: 10000, giam: 5000) -> 15000`. Thấy kết quả xanh mướt trước mắt rồi mới yên tâm viết tiếp hàm thứ hai. Nếu có lỗi, bạn biết chắc chắn 100% lỗi chỉ nằm trong 5 dòng code bạn vừa gõ!

### 1.2. Cái giá đắt đỏ của việc phát hiện lỗi muộn

- **Lỗi phát hiện sau 10 giây (khi đang gõ):** Sửa mất 5 giây (sửa 1 dấu chấm phẩy, đổi dấu `>` thành `>=`).
- **Lỗi phát hiện sau 2 tiếng (khi ghép cả UI):** Sửa mất 45 phút (phải debug lần mò qua 4 tầng code xem tại sao số tiền hiển thị trên bảng bị sai).
- **Lỗi phát hiện khi đã lên môi trường Production (khách hàng dùng):** Mất tiền, mất uy tín, rollback hệ thống khẩn cấp trong đêm.

---

## 2. Tư duy "Chia để trị": Bóc tách ứng dụng thành 4 tầng để test

Để có thể test từng phần ngay lập tức, nguyên tắc số 1 là: **Đừng coi ứng dụng là một mớ bòng bong dính chặt vào nhau.** Một ứng dụng chuẩn luôn chia thành các tầng độc lập:

```
+-------------------------------------------------------------+------------------------------------+
| TẦNG ỨNG DỤNG                                               | CÁCH TEST NGAY LẬP TỨC             |
+-------------------------------------------------------------+------------------------------------+
| [Tầng 4] Giao diện (Swing UI, REST Controller)             | UI chỉ hiển thị dữ liệu (Dumb View)|
|          Chỉ nhận tương tác chuột / bàn phím               | Không chứa logic nghiệp vụ nặng    |
+-------------------------------------------------------------+------------------------------------+
| [Tầng 3] Nghiệp vụ (Business Service / Workflow)            | Dùng Unit Test + Mockito           |
|          Quy tắc kiểm tra tồn kho, hạn mức nợ              | Giả lập DAO trả kết quả mong muốn  |
+-------------------------------------------------------------+------------------------------------+
| [Tầng 2] Dữ liệu (DAO / JDBC / SQL Queries)                | Chạy trực tiếp qua Test Harness    |
|          Gọi Stored Function / Procedure xuống DB           | Tự Rollback không làm bẩn DB       |
+-------------------------------------------------------------+------------------------------------+
| [Tầng 1] Thuần Logic & Tính toán (Model / Utils)            | Test cực nhanh: JShell hoặc JUnit  |
|          CartLine.lineTotal(), tính thuế, format ngày       | 0 phụ thuộc, chạy trong 0.01 giây! |
+-------------------------------------------------------------+------------------------------------+
```

### Tầng 1: Thuần Logic & Tính toán (Model, Validator, Helper) — Test trong 0.01s
- **Đặc điểm:** Hoàn toàn **KHÔNG** kết nối Database, **KHÔNG** có giao diện người dùng, **KHÔNG** đọc ghi file. Chỉ nhận dữ liệu đầu vào và trả ra kết quả (Pure Functions).
- **Ví dụ trong dự án:**
  - `CartLine.lineTotal()`: Lấy `(quantity * unitPrice) - discount`.
  - Hàm kiểm tra số điện thoại hợp lệ: `Validator.isValidPhone(phone)`.
  - Hàm làm tròn tiền tệ sang `BigDecimal`: `MoneyUtils.round(amount)`.
- **Cách test ngay:** Dùng ngay **JUnit 5** hoặc **JShell**. Viết test xong bấm Run, kết quả hiện ra trong tích tắc!

### Tầng 2: Tầng dữ liệu (DAO / Database Access) — Test độc lập không cần bật UI
- **Đặc điểm:** Tầng này gửi câu lệnh SQL / Stored Procedure xuống MySQL và ánh xạ dữ liệu về Java Object.
- **Nỗi đau nếu không biết cách test:** Nhiều bạn dev muốn test xem hàm `ProductDao.priceOn("8934008", date)` có lấy đúng giá hay không, liền: Bật ứng dụng Swing lên -> Chọn quầy -> Chọn thu ngân -> Chọn khách hàng -> Thêm sản phẩm vào bảng -> Nhìn xem giá có hiện không. **Mất đứt 1 phút cho 1 lần kiểm tra!**
- **Cách test ngay:** Viết 1 file test độc lập gọi thẳng `new ProductDao().priceOn(...)`. Bấm Run mất đúng **0.2 giây**!

### Tầng 3: Tầng nghiệp vụ (Service / Business Rules) — Cô lập bằng Mocking
- **Đặc điểm:** Chứa logic phối hợp nhiều DAO (ví dụ: tạo hóa đơn thì phải trừ tồn kho, nếu trừ thất bại thì dừng).
- **Cách test ngay:** Dùng thư viện **Mockito** để "đóng giả" Database/DAO. Bạn có thể ép DAO giả vờ ném ra lỗi `SQLException` để xem tầng nghiệp vụ của bạn có xử lý rollback đúng cách hay không, mà không cần phải thực sự rút dây mạng hay tắt MySQL!

### Tầng 4: Tầng giao diện (Swing UI / Web Controller) — Giữ UI "ngu ngơ"
- **Nguyên tắc vàng:** **Dumb UI (Giao diện ngu ngơ / View mỏng).**
- Đừng bao giờ viết code tính tiền, trừ tiền, kiểm tra tồn kho bên trong sự kiện bấm nút `btnSave.addActionListener(...)`!
- Tầng giao diện chỉ làm đúng 2 việc:
  1. Lấy chuỗi người dùng gõ trên màn hình gửi xuống cho tầng Service/DAO.
  2. Lấy kết quả từ Service/DAO hiển thị lên bảng `JTable` hoặc nhãn `JLabel`.
- Khi toàn bộ logic tính toán đã được test sạch sẽ ở Tầng 1, 2, 3 rồi, thì Tầng 4 gần như không bao giờ có lỗi logic!

---

## 3. 5 "Vũ khí" test ngay lập tức trong Java (Từ 0 đồng đến Chuyên nghiệp)

Dưới đây là 5 công cụ từ nhẹ nhất (có sẵn trong máy) đến chuyên nghiệp nhất mà mọi Java dev bắt buộc phải nằm lòng:

---

### Vũ khí 1: JShell (Java REPL) — Thử nghiệm biểu thức trong 1 giây

Từ phiên bản Java 9 trở đi, JDK tích hợp sẵn một công cụ dòng lệnh cực kỳ vi diệu gọi là **JShell** (Read-Eval-Print Loop). Bạn không cần tạo class, không cần tạo hàm `main`, không cần biên dịch `javac`.

#### Khi nào dùng?
Khi bạn vừa gõ một biểu thức tính toán `BigDecimal`, một câu Regex so khớp chuỗi, hoặc một phép tính ngày tháng `LocalDate` mà không chắc Java có chạy đúng như mình nghĩ không.

#### Cách dùng:
1. Mở Terminal (PowerShell hoặc Bash), gõ:
   ```bash
   jshell
   ```
2. Gõ ngay code Java và nhấn `Enter`:
   ```java
   jshell> import java.math.BigDecimal;
   jshell> import java.math.RoundingMode;

   jshell> BigDecimal qty = new BigDecimal("2.5");
   jshell> BigDecimal price = new BigDecimal("130000");
   jshell> BigDecimal discount = new BigDecimal("15000");

   jshell> qty.multiply(price).subtract(discount).setScale(2, RoundingMode.HALF_UP)
   $5 ==> 310000.00
   ```
3. Bạn thấy kết quả `$5 ==> 310000.00` hiện ra ngay trước mắt trong **0.1 giây**!
4. Muốn thoát JShell: gõ `/exit`.

---

### Vũ khí 2: IntelliJ Scratch Files & Shortcuts — Chạy thử code nháp độc lập

Nếu bạn dùng IntelliJ IDEA, bạn có một tính năng vô cùng đáng giá: **Scratch Files**.

- **Cách mở nhanh:** Nhấn tổ hợp phím:
  - Windows/Linux: `Ctrl + Alt + Shift + Insert`
  - macOS: `Cmd + Shift + N`
- Chọn loại file: **Java**.
- IntelliJ sẽ tạo ra một file `Scratch.java` nằm riêng biệt, không liên quan đến project chính.
- Bạn có thể `import billing.model.CartLine;`, tạo đối tượng và bấm nút tam giác xanh ▶️ để chạy thử ngay lập tức!
- Thử nghiệm xong thì xóa hoặc để đó làm ghi chú, không sợ làm bẩn git hay mã nguồn dự án.

---

### Vũ khí 3: Quick Test Harness (`main` runner riêng biệt) — Siêu tốc, không cần cài đặt

Trong các dự án Java thuần không dùng Maven/Gradle phức tạp (chính là project `DemoBillingManagement` này), cách nhanh nhất để test ngay một class vừa viết là: **Tạo một class chạy thử nghiệm có hàm `main` độc lập**.

> **Dự án này đã có sẵn một file như vậy:** [`src/billing/test/QuickTestRunner.java`](../src/billing/test/QuickTestRunner.java). Mở ra, bấm *Run* là thấy ngay bảng kết quả ✅/❌ cho cả tầng Model lẫn tầng DAO. Đoạn dưới đây giải thích cách nó được viết ra, để bạn tự làm một cái tương tự cho class của mình.

#### Ví dụ: Bạn vừa viết xong `CartLine.java`, hãy tạo ngay `QuickTest.java`:

```java
package billing.test;

import billing.model.CartLine;
import billing.model.Product;
import java.math.BigDecimal;

public class QuickTest {
    public static void main(String[] args) {
        System.out.println("=== BẮT ĐẦU KIỂM THỬ CARTLINE ===");

        Product p = new Product("8934001", "Sua tuoi Vinamilk 1L", "LITER", new BigDecimal("8.00"));
        
        // Ca 1: Tính tiền bình thường: 2 hộp * 20.000đ - 5.000đ giảm giá = 35.000đ
        CartLine line1 = new CartLine(p, new BigDecimal("2"), new BigDecimal("20000"), 
                                      new BigDecimal("5000"), "Giam 5k");
        BigDecimal total1 = line1.lineTotal();
        check("Ca 1 - Tinh tien chuan", total1.compareTo(new BigDecimal("35000.00")) == 0, 
              "Mong muon: 35000.00, Thuc te: " + total1);

        // Ca 2: Giam gia lon hon ca tien hang -> Tien co bi am khong?
        CartLine line2 = new CartLine(p, new BigDecimal("1"), new BigDecimal("10000"), 
                                      new BigDecimal("15000"), "Giam qua tay");
        BigDecimal total2 = line2.lineTotal();
        check("Ca 2 - Giam vuot qua gia", total2.compareTo(BigDecimal.ZERO) >= 0,
              "CANH BAO: Tien dong bi am! Thuc te: " + total2);

        System.out.println("=== KẾT THÚC KIỂM THỬ ===");
    }

    private static void check(String testName, boolean condition, String errorDetail) {
        if (condition) {
            System.out.println("✅ [PASS] " + testName);
        } else {
            System.err.println("❌ [FAIL] " + testName + " -> " + errorDetail);
        }
    }
}
```

- **Thời gian chạy:** **0.3 giây**.
- **Kết quả hiển thị:**
  ```text
  === BẮT ĐẦU KIỂM THỬ CARTLINE ===
  ✅ [PASS] Ca 1 - Tinh tien chuan
  ❌ [FAIL] Ca 2 - Giam vuot qua gia -> CANH BAO: Tien dong bi am! Thuc te: -5000.00
  === KẾT THÚC KIỂM THỬ ===
  ```
- **Nhìn thấy ngay lỗi logic:** Bạn lập tức phát hiện ra: *"Ối! Nếu giảm giá lớn hơn tiền hàng thì `lineTotal()` trả về số âm! Hóa đơn siêu thị mà dòng tiền âm thì sập hệ thống kế toán!"*.
- Bạn mở ngay `CartLine.java` ra sửa — và đây **chính là đoạn code đang nằm trong dự án lúc này**:
  ```java
  public BigDecimal lineTotal() {
      if (quantity == null || unitPrice == null) {
          return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
      }
      BigDecimal safeDiscount = (discount != null) ? discount : BigDecimal.ZERO;
      BigDecimal total = quantity.multiply(unitPrice).subtract(safeDiscount);
      if (total.compareTo(BigDecimal.ZERO) < 0) {
          total = BigDecimal.ZERO;          // Không cho phép âm
      }
      return total.setScale(2, RoundingMode.HALF_UP);
  }
  ```
- Chạy lại `QuickTest`: Cả 2 ca đều **✅ [PASS]**. Bạn sửa xong lỗi logic chỉ trong 30 giây, trước khi ghép vào giao diện!

---

### Vũ khí 4: Tiêu chuẩn vàng JUnit 5 — Bấm nút xanh 🟢 thấy kết quả trong 0.2s

**JUnit 5 (Jupiter)** là thư viện kiểm thử tiêu chuẩn quốc tế số 1 của thế giới Java. Mọi IDE lớn (IntelliJ, Eclipse, VS Code, NetBeans) đều hỗ trợ sẵn giao diện trực quan cho JUnit.

#### Phím tắt thần thánh trong IntelliJ IDEA:
Khi bạn đang mở bất kỳ file Java nào (ví dụ `CartLine.java`):
- Nhấn `Ctrl + Shift + T` (Windows) hoặc `Cmd + Shift + T` (macOS).
- Chọn **Create New Test...** -> Chọn **JUnit 5**.
- IDE tự động sinh ra file `CartLineTest.java` đặt trong thư mục test!

#### Cách viết một Unit Test chuẩn cấu trúc AAA (Arrange - Act - Assert):

```java
import billing.model.CartLine;
import billing.model.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CartLineTest {

    @Test
    @DisplayName("Thành tiền phải bằng (số lượng * đơn giá) trừ khuyến mãi")
    void testLineTotalCalculation() {
        // 1. ARRANGE (Chuẩn bị dữ liệu)
        Product p = new Product("8934002", "Banh mi sandwich", "PACK", new BigDecimal("8.00"));
        BigDecimal quantity = new BigDecimal("3");
        BigDecimal unitPrice = new BigDecimal("25000");
        BigDecimal discount = new BigDecimal("5000");
        CartLine line = new CartLine(p, quantity, unitPrice, discount, "Giảm 5k cho khách quen");

        // 2. ACT (Gọi hàm cần kiểm thử)
        BigDecimal result = line.lineTotal();

        // 3. ASSERT (Khẳng định kết quả mong đợi)
        // 3 * 25.000 - 5.000 = 70.000
        BigDecimal expected = new BigDecimal("70000.00");
        assertEquals(expected, result, "Thành tiền tính ra không chính xác!");
    }

    @Test
    @DisplayName("Nếu số lượng là 0 thì thành tiền phải bằng 0")
    void testLineTotalWithZeroQuantity() {
        Product p = new Product("8934002", "Banh mi sandwich", "PACK", new BigDecimal("8.00"));
        CartLine line = new CartLine(p, BigDecimal.ZERO, new BigDecimal("25000"), BigDecimal.ZERO, null);

        assertEquals(new BigDecimal("0.00"), line.lineTotal());
    }

    @Test
    @DisplayName("Khi số lượng bị null thì trả về 0.00, không được ném NullPointerException")
    void testLineTotalWithNullQuantity() {
        Product p = new Product("8934002", "Banh mi sandwich", "PACK", new BigDecimal("8.00"));
        CartLine line = new CartLine(p, null, new BigDecimal("25000"), BigDecimal.ZERO, null);

        // CartLine hiện tại chọn cách "chịu đựng": trả 0 thay vì làm sập app
        assertEquals(new BigDecimal("0.00"), line.lineTotal());
    }

    @Test
    @DisplayName("Giảm giá lớn hơn tiền hàng thì thành tiền phải bằng 0, không được âm")
    void testLineTotalNeverNegative() {
        Product p = new Product("8934002", "Banh mi sandwich", "PACK", new BigDecimal("8.00"));
        CartLine line = new CartLine(p, BigDecimal.ONE, new BigDecimal("10000"),
                                     new BigDecimal("15000"), "Voucher 15k");

        assertEquals(0, line.lineTotal().compareTo(BigDecimal.ZERO));
    }
}
```

> **Một quyết định thiết kế bạn phải tự chọn:** khi `quantity` bị `null`, nên **trả về 0** hay nên **ném exception**? Trả về 0 thì app không sập nhưng lỗi bị giấu đi (thu ngân thấy hóa đơn 0đ mà không hiểu vì sao); ném `IllegalArgumentException` thì lỗi lộ ra ngay nhưng phải có chỗ bắt. `CartLine` trong dự án này chọn phương án **trả về 0** — nên bài test ở trên viết theo đúng lựa chọn đó.
>
> Điều quan trọng không phải chọn phương án nào, mà là: **test phải mô tả đúng hành vi mà code cam kết**. Một bài test `assertThrows(...)` trong khi code trả về 0 sẽ đỏ mãi mãi và chẳng chứng minh được gì.

> **Muốn chạy được JUnit thì cần gì?** Bộ script `run.ps1` / `run.sh` chỉ dùng `javac` thuần nên **không** có sẵn thư viện JUnit. Hãy mở dự án bằng IntelliJ/Eclipse qua `pom.xml` — file này đã khai báo sẵn `junit-jupiter` ở `<scope>test</scope>`, IDE sẽ tự tải về. Không muốn dùng Maven thì dùng **Vũ khí 3** (`QuickTestRunner`), nó chạy được bằng `javac` thuần.

#### Trải nghiệm trực quan:
- Bên cạnh mỗi phương thức `@Test` có một **icon tam giác xanh ▶️**.
- Bạn click chuột vào icon đó:
  - Nếu đúng: IDE hiện thanh xanh lá cây **PASSED (12 ms)** ✅. Cảm giác cực kỳ sướng và tự tin!
  - Nếu sai: IDE hiện thanh đỏ rực **FAILED** ❌ kèm thông báo đối chiếu rõ ràng:
    ```text
    Expected: 70000.00
    Actual  : 75000.00
    at CartLineTest.testLineTotalCalculation(CartLineTest.java:24)
    ```
  - Bạn chỉ cần bấm vào dòng chữ màu xanh là nhảy ngay tới dòng code bị lỗi!

---

### Vũ khí 5: Giả lập thế giới bên ngoài với Mockito (Mocking)

#### Vấn đề:
Giả sử bạn đang viết lớp `InvoiceService` chứa nghiệp vụ: *"Khi thanh toán, nếu tài khoản thẻ không đủ tiền thì ném ra lỗi `PaymentException` và không cho phép ghi vào Database"*.

Làm sao để test được trường hợp thẻ ngân hàng bị từ chối?
- Cách nghiệp dư: Phải đi tìm một chiếc thẻ ngân hàng hết tiền thật để quẹt thử!
- Cách chuyên nghiệp: Dùng **Mockito** để tạo ra một đối tượng giả mạo (Mock). Bạn thích nó trả về cái gì, nó sẽ trả về cái đó!

```java
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Test;

class InvoiceServiceTest {

    @Test
    void testPaymentFailedRollsBack() {
        // Tạo các đối tượng giả mạo, hoàn toàn không cần kết nối MySQL
        PaymentGateway mockGateway   = mock(PaymentGateway.class);
        InvoiceDao     mockInvoiceDao = mock(InvoiceDao.class);

        // Ra lệnh cho kẻ giả mạo: "Hễ ai hỏi thẻ 1234 thì hãy bảo là HẾT TIỀN"
        when(mockGateway.charge("1234", new BigDecimal("500000")))
            .thenReturn(PaymentStatus.INSUFFICIENT_FUNDS);

        InvoiceService service = new InvoiceService(mockGateway, mockInvoiceDao);

        // Kiểm tra xem service có bắt đúng lỗi hết tiền hay không
        assertThrows(PaymentException.class, () -> {
            service.processCheckout("1234", new BigDecimal("500000"));
        });

        // Xác nhận cổng thanh toán đã được hỏi ĐÚNG MỘT LẦN (không gọi lặp, không bỏ sót)
        verify(mockGateway, times(1)).charge("1234", new BigDecimal("500000"));

        // Và xác nhận KHÔNG hề ghi gì xuống database sau khi thẻ bị từ chối
        verify(mockInvoiceDao, never()).createInvoice(any(), any(), any(),
                                                      any(), any(), any(), any(), any());
    }
}
```

> **Lưu ý về ví dụ trên:** `InvoiceService`, `PaymentGateway`, `PaymentStatus` là các lớp **giả định để minh họa** — dự án `DemoBillingManagement` hiện chưa có tầng Service, tầng UI gọi thẳng xuống DAO. Đây là mẫu code bạn sẽ dùng khi dự án lớn lên và cần tách tầng nghiệp vụ ra.
>
> `pom.xml` của dự án cũng **chưa khai báo Mockito** (mới chỉ có `junit-jupiter`). Muốn thử, thêm dependency `org.mockito:mockito-core` với `<scope>test</scope>` trước đã.

> **Bài học rút ra:** Mockito giúp bạn kiểm thử được cả những kịch bản ngặt nghèo nhất (mất mạng, database sập, tài khoản âm, hết hàng trong kho) chỉ trong 0.1 giây mà không cần tác động đến môi trường thật!

---

## 4. Thực hành chi tiết trên codebase DemoBillingManagement

Hãy áp dụng ngay các kỹ thuật trên vào chính dự án này để thấy sự khác biệt một trời một vực giữa dev có test và dev không có test.

---

### Thực hành 1: Bắt lỗi logic tính tiền trong `CartLine`

Đây là bản **đầu tiên** của `lineTotal()` trong file [`src/billing/model/CartLine.java`](../src/billing/model/CartLine.java), trước khi có bài test nào:

```java
// PHIÊN BẢN CŨ -- đã được sửa, giữ lại đây để thấy 3 lỗ hổng
public BigDecimal lineTotal() {
    return quantity.multiply(unitPrice)
                   .subtract(discount)
                   .setScale(2, RoundingMode.HALF_UP);
}
```

Hãy nhìn kỹ đoạn code trên. Trông thì có vẻ rất bình thường và ngắn gọn. Nhưng nếu không test từng phần, bạn sẽ bỏ sót ít nhất **3 quả bom nổ chậm**:

1. **Quả bom 1: `discount` bị `null`:**
   Nếu người dùng chọn một sản phẩm không có chương trình khuyến mãi nào, `discount` có thể được truyền vào là `null` hoặc người dùng quên khởi tạo. Khi gọi `.subtract(discount)` -> **`NullPointerException` làm sập app ngay lập tức!**
2. **Quả bom 2: Giảm giá lớn hơn tiền hàng:**
   Nếu thu ngân nhập voucher giảm 100.000đ cho đơn hàng 80.000đ -> Kết quả trả về `-20000.00`. Khi lưu xuống SQL, cột `Line_Total DECIMAL(12,2)` sẽ lưu số âm, dẫn tới tổng tiền hóa đơn bị sai lệch nghiêm trọng.
3. **Quả bom 3: Số lượng lẻ và làm tròn:**
   Sản phẩm bán theo kg (ví dụ thịt heo `quantity = 1.345 kg`). Nếu không kiểm tra quy tắc làm tròn `HALF_UP` thì đơn giá nhân lên có thể bị lệch 1 đồng so với máy tính bỏ túi của khách hàng.

#### Giải pháp khắc phục ngay sau khi test — và đây là code **đang chạy thật** trong dự án:
```java
/**
 * Thành tiền = số lượng * đơn giá - giảm giá
 * Ràng buộc nghiệp vụ: Không âm và an toàn với null.
 */
public BigDecimal lineTotal() {
    if (quantity == null || unitPrice == null) {
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }
    BigDecimal safeDiscount = (discount != null) ? discount : BigDecimal.ZERO;
    BigDecimal total = quantity.multiply(unitPrice).subtract(safeDiscount);
    if (total.compareTo(BigDecimal.ZERO) < 0) {
        total = BigDecimal.ZERO;
    }
    return total.setScale(2, RoundingMode.HALF_UP);
}
```

Ba quả bom đã được tháo:

| Quả bom | Cách xử lý đã chọn |
|---|---|
| `discount` bị `null` | Coi như bằng 0 (`safeDiscount`) |
| Giảm giá > tiền hàng | Kẹp sàn về `0`, không bao giờ trả số âm |
| `quantity` / `unitPrice` bị `null` | Trả `0.00` thay vì để `NullPointerException` làm sập app |

> **Lưu ý:** trả `0.00` khi gặp `null` là một **lựa chọn**, không phải chân lý. Xem phần thảo luận ở cuối Vũ khí 4 về việc "trả 0" hay "ném exception" — điều bắt buộc là bài test phải mô tả đúng lựa chọn mà code đã cam kết.

Nhờ test ngay từ tầng Model, bạn đã triệt tiêu toàn bộ lỗi tiềm ẩn trước khi bất kỳ ai kịp nhìn thấy giao diện!

---

### Thực hành 2: Test Stored Function `priceOn()` trong `ProductDao` mà không cần bật UI

Xem file [`src/billing/dao/ProductDao.java`](../src/billing/dao/ProductDao.java), hàm:
```java
public BigDecimal priceOn(String barcode, LocalDate date)
```
Hàm này gọi Stored Function `fn_get_product_price(?, ?)` dưới MySQL để lấy giá của sản phẩm tại một ngày bất kỳ trong quá khứ hoặc hiện tại.

#### Cách kiểm tra siêu tốc (Fast Verification):
Không cần bật app Swing, hãy tạo một class test nhỏ hoặc chạy một đoạn script:

```java
package billing.test;

import billing.dao.ProductDao;
import java.math.BigDecimal;
import java.time.LocalDate;

public class ProductDaoTest {
    public static void main(String[] args) {
        ProductDao dao = new ProductDao();

        // Mã vạch thịt ba chỉ trong 02_seed.sql là 8934008 (mã 7 số, không phải 13 số)
        String barcode = "8934008";

        // 1. Giá hôm nay (giá mới trong seed): 158.000đ
        BigDecimal priceToday = dao.priceOn(barcode, LocalDate.now());
        check("Giá hôm nay", priceToday, new BigDecimal("158000"));

        // 2. Giá 400 ngày trước (giá cũ trong seed): 130.000đ
        BigDecimal priceOld = dao.priceOn(barcode, LocalDate.now().minusDays(400));
        check("Giá 400 ngày trước", priceOld, new BigDecimal("130000"));

        // 3. Mã không tồn tại: fn_get_product_price trả NULL -> Java nhận null
        BigDecimal priceFake = dao.priceOn("9999999", LocalDate.now());
        System.out.println((priceFake == null ? "✅" : "❌") + " Mã giả trả về: " + priceFake);
    }

    /** Tự viết hàm so sánh -- KHÔNG dùng từ khóa `assert` (xem cảnh báo bên dưới). */
    private static void check(String ten, BigDecimal thucTe, BigDecimal mongDoi) {
        boolean ok = thucTe != null && thucTe.compareTo(mongDoi) == 0;
        System.out.println((ok ? "✅" : "❌") + " " + ten
                + " -- mong đợi " + mongDoi + ", thực tế " + thucTe);
    }
}
```

> ⚠️ **Cái bẫy `assert` mà rất nhiều người dính:** từ khóa `assert` của Java **bị tắt mặc định** khi chạy. Viết `assert price != null;` rồi bấm Run, bạn sẽ thấy "chạy êm ru" — nhưng thật ra dòng đó **không hề được thực thi**. Muốn bật, phải thêm cờ `-ea` (enable assertions) vào lệnh `java`, hoặc vào *Run Configuration* của IDE thêm VM option `-ea`.
>
> Vì vậy trong một `main()` chạy nhanh, hãy **tự viết hàm `check(...)` in ra ✅/❌** như trên (đúng cách mà `QuickTestRunner` đang làm), hoặc dùng hẳn `assertEquals` của JUnit — cả hai đều không phụ thuộc vào cờ `-ea`.

> **Hai con số 158.000 / 130.000 lấy từ đâu?** Từ `02_seed.sql`. Nếu bạn sửa file seed thì phải sửa kỳ vọng trong test theo. Ngày tháng trong seed được tính tương đối theo `CURDATE()`, nhưng hai mức giá thì cố định nên test này chạy lúc nào cũng đúng.

Bấm chạy: **0.2 giây sau bạn có kết quả.** Bạn biết chắc chắn 100% câu lệnh JDBC `CallableStatement` kết nối tới MySQL và gọi hàm `fn_get_product_price` hoàn toàn chính xác!

---

### Thực hành 3: Test Transaction an toàn với Rollback trong `InvoiceDao`

Nỗi sợ lớn nhất của dev khi test các hàm ghi dữ liệu (INSERT, UPDATE, DELETE) là: **"Sợ làm bẩn hoặc làm hỏng dữ liệu trong Database thật"**.

#### Bí quyết của chuyên gia: "Luôn luôn Rollback trong Test"!

Nguyên tắc chung gồm 4 bước:
1. Mở kết nối Database: `Connection conn = Db.getConnection();`
2. Tắt chế độ tự động lưu: `conn.setAutoCommit(false);`
3. Thực thi các thao tác ghi thử nghiệm **trên chính `conn` đó**.
4. **Ở khối `finally`: Luôn gọi `conn.rollback()`!**

```java
@Test
void testGhiDuLieuRoiRollback() throws SQLException {
    try (Connection conn = Db.getConnection()) {
        // TẮT AUTO-COMMIT: Mọi thứ chỉ nằm trong bộ nhớ đệm của transaction này
        conn.setAutoCommit(false);

        try (CallableStatement cs =
                 conn.prepareCall("{call sp_create_invoice_header(?, ?, ?, ?, ?, ?)}")) {

            cs.setString(1, "INV-TEST-01");
            cs.setDate  (2, java.sql.Date.valueOf(LocalDate.now()));
            cs.setTime  (3, java.sql.Time.valueOf(LocalTime.now()));
            cs.setString(4, "C01");
            cs.setString(5, "E01");
            cs.setNull  (6, java.sql.Types.VARCHAR);   // khách vãng lai
            cs.executeUpdate();

            // Đọc lại NGAY TRONG transaction để xác nhận đã ghi được
            try (PreparedStatement ps = conn.prepareStatement(
                     "SELECT COUNT(*) FROM Invoice WHERE Invoice_ID = ?")) {
                ps.setString(1, "INV-TEST-01");
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    assertEquals(1, rs.getInt(1), "Hóa đơn thử nghiệm phải được ghi");
                }
            }
        } finally {
            // ROLLBACK NGAY LẬP TỨC: Database quay về trạng thái ban đầu 100% sạch sẽ!
            conn.rollback();
            System.out.println("Đã Rollback transaction. Database hoàn toàn nguyên vẹn!");
        }
    }
}
```

> **Lợi ích thần kỳ:** Bạn có thể chạy test này 1.000 lần liên tục mà database không hề bị thêm bất kỳ một dòng rác nào!

#### Vì sao không test thẳng `InvoiceDao.createInvoice(...)` theo cách này?

Hãy mở [`InvoiceDao.java`](../src/billing/dao/InvoiceDao.java) ra xem chữ ký của nó:

```java
public void createInvoice(String invoiceId, LocalDate date, LocalTime time,
                          String counterId, String cashierId, String customerId,
                          List<CartLine> lines, List<PaymentEntry> payments)
```

Ba điều đáng chú ý:

1. Nó **không nhận `Connection` từ bên ngoài** — nó tự gọi `Db.getConnection()` bên trong.
2. Nó **tự `commit()`** ở cuối. Bạn `rollback()` bên ngoài cũng không rút lại được gì, vì transaction đã chốt xong rồi.
3. Nó **trả về `void`**, và mã hóa đơn là chuỗi dạng `INV-0001` do `fn_next_invoice_id()` sinh ra — không phải số nguyên tự tăng.

Vậy nên có đúng hai cách test hàm này:

- **Cách nhanh (không phải sửa code):** gọi thật, rồi tự dọn bằng `new InvoiceDao().deleteInvoice(id)` trong khối `finally`. Chấp nhận là database bị chạm vào thật trong vài mili-giây. (`QuickTestRunner` hiện chưa có ca test này — đây là bài tập tốt để bạn tự thêm vào.)
- **Cách sạch (nếu muốn làm đúng bài bản):** refactor `createInvoice` thành hai tầng — một hàm `createInvoice(Connection conn, ...)` chỉ lo nghiệp vụ, và một hàm public mở/đóng connection rồi gọi vào nó. Lúc đó bài test mới truyền được connection của riêng mình vào và rollback thoải mái.

> **Đây chính là bài học lớn nhất của cả tài liệu này:** *code khó test thường là code đang thiếu một đường nối*. Khi bạn thấy "không sao test nổi hàm này", 90% là do hàm đó tự đi tìm tài nguyên của nó (tự mở connection, tự đọc file, tự lấy giờ hệ thống) thay vì **nhận vào từ bên ngoài**. Kỹ thuật sửa gọi là *Dependency Injection*, và đó cũng là lý do Spring tồn tại.

---

## 5. Checklist các "Ca biên" (Edge Cases) bắt sạch lỗi logic

Khi viết code, người bình thường chỉ nghĩ đến trường hợp lý tưởng (**Happy Path** — người dùng ngoan ngoãn nhập đúng số). Nhưng 90% lỗi trong thực tế lại đến từ các **ca biên (Edge Cases)**. 

Hãy luôn cầm checklist này trong tay và viết test cho chúng trước khi bàn giao code:

### 1. Số học & Tiền bạc (`BigDecimal`, `int`, `double`)
- [ ] **Số 0:** Số lượng = 0, đơn giá = 0, giảm giá = 0. App có bị chia cho 0 (`ArithmeticException`) không?
- [ ] **Số âm:** Số lượng âm (`-5`), đơn giá âm. Có bị lợi dụng để rút tiền từ hệ thống không?
- [ ] **Lớn hơn tổng:** Tiền giảm giá > Tổng tiền hàng.
- [ ] **Số thập phân siêu lẻ:** `1.333333...`. Đã cấu hình `RoundingMode` rõ ràng chưa? (Tránh lỗi `ArithmeticException: Non-terminating decimal expansion`).
- [ ] **Số cực lớn:** Tổng tiền vượt quá `999.999.999.999`. Có bị tràn cột `DECIMAL(12,2)` không?

### 2. Chuỗi ký tự (`String`)
- [ ] **`null`:** Chuỗi bị null (rất hay gặp khi lấy từ database hoặc người dùng bỏ trống).
- [ ] **Chuỗi rỗng:** `""`.
- [ ] **Chuỗi toàn khoảng trắng:** `"   "`. Có dùng `.trim().isEmpty()` để kiểm tra không?
- [ ] **Chuỗi siêu dài:** Tên sản phẩm dài 500 ký tự trong khi cột SQL chỉ cho phép `VARCHAR(100)`.
- [ ] **Ký tự đặc biệt:** Dấu nháy đơn `'`, nháy kép `"`, dấu gạch chéo `/`, emoji `🍔`, tiếng Việt có dấu.

### 3. Thời gian (`LocalDate`, `LocalDateTime`)
- [ ] **Ngày trong quá khứ:** Áp dụng khuyến mãi cho ngày hôm qua (khuyến mãi đã hết hạn).
- [ ] **Ngày trong tương lai:** Đặt lịch trước.
- [ ] **Ngày biên:** Khuyến mãi kết thúc lúc `23:59:59` của ngày hôm nay. Đúng `00:00:00` ngày mai có còn được giảm giá không?
- [ ] **Năm nhuận:** Ngày `29/02`.

### 4. Danh sách & Tập hợp (`List`, `Set`, `Map`)
- [ ] **Danh sách rỗng:** Giỏ hàng không có sản phẩm nào mà bấm nút "Thanh toán".
- [ ] **Danh sách có 1 phần tử:** Trường hợp đơn giản nhất.
- [ ] **Danh sách có 10.000 phần tử:** Hiệu năng có bị đơ màn hình không?
- [ ] **Trùng lặp:** Quét cùng 1 mã barcode 2 lần thì app tạo 2 dòng riêng hay tự cộng dồn số lượng lên?

---

## 6. Tư duy TDD thực dụng (Pragmatic Test-Driven Development)

Nhiều tài liệu dạy TDD (Phát triển hướng kiểm thử) theo kiểu giáo điều, bắt bạn phải tuân thủ nghiêm ngặt mọi quy tắc khiến bạn cảm thấy nặng nề. Nhưng trong thực chiến, hãy áp dụng **TDD Thực dụng (Pragmatic TDD)** với chu trình 3 bước siêu đơn giản:

```
    [1. RED (Đỏ)] 
    Viết 1 hàm test mô tả kết quả mong muốn
    Bấm Run -> Báo đỏ (vì chưa viết logic)
             ↓
    [2. GREEN (Xanh)]
    Viết lượng code vừa đủ để hàm test vượt qua
    Bấm Run -> Báo xanh mướt!
             ↓
    [3. REFACTOR (Dọn dẹp)]
    Sắp xếp lại code cho gọn gàng, đặt tên biến đẹp hơn
    Bấm Run -> Vẫn xanh! Yên tâm 100% không làm hỏng code cũ!
```

### Vì sao cách này giúp bạn dev nhanh gấp đôi?
1. **Không phải suy nghĩ mông lung:** Khi viết test trước, não bạn được định hình rõ ràng: *"Hàm này nhận vào A, phải trả ra B"*. Bạn không bị lan man sang các việc khác.
2. **"Lưới bảo hiểm" trọn đời:** Sau này khi bạn sửa lại code (Refactor) hoặc tối ưu thuật toán, bạn không cần phải mở app bấm tay kiểm tra lại từ đầu. Chỉ cần bấm 1 phím tắt chạy bộ test: nếu toàn bộ xanh lá cây nghĩa là bạn không làm hỏng bất kỳ tính năng cũ nào!

---

## 7. Bảng đối chiếu: Dev "Cầu nguyện" vs Dev "Fast Feedback"

| Tiêu chí | Dev "Viết một lèo rồi cầu nguyện" | Dev "Fast Feedback Loop" (Chuyên nghiệp) |
|---|---|---|
| **Thời gian thấy kết quả** | 1 đến 2 tiếng sau khi ghép hết các tầng | **30 giây đến 1 phút** sau khi viết xong phương thức |
| **Cách tìm lỗi** | Chèn `System.out.println` vô tội vạ, đặt breakpoint debug mò mẫm | Nhìn thẳng vào thông báo đỏ của Test: biết chính xác dòng nào sai |
| **Khi app bị lỗi** | Hoang mang không biết lỗi tại SQL, JDBC, Java hay UI | Tự tin 100%: Tầng dưới đã test xanh, lỗi chắc chắn chỉ ở dòng vừa viết |
| **Tâm lý khi code** | Lo lắng, sợ hãi mỗi lần bấm Run toàn ứng dụng | Nhẹ nhàng, thư thái, cảm giác thỏa mãn mỗi lần thấy tick xanh ✅ |
| **Độ phủ ca biên** | Chỉ test được 1-2 ca lý tưởng khi bấm tay trên giao diện | Test tự động hàng chục ca biên (âm, rỗng, null) trong nháy mắt |
| **Khi cần sửa code cũ** | Sợ run tay, không dám đụng vì sợ hỏng tính năng khác | Bấm Run Test: Xanh = An toàn, Đỏ = Sửa ngay |

---

## 8. Tổng kết: 4 thói quen vàng của Java Developer chuyên nghiệp

Để không bao giờ rơi vào cái bẫy "viết một lèo rồi sửa mệt nghỉ", hãy khắc cốt ghi tâm 4 thói quen sau:

1. **Phím tắt kiểm thử luôn nằm trong ngón tay:**
   - Thuộc lòng phím tắt tạo và chạy test trong IDE của bạn (`Ctrl + Shift + T` trong IntelliJ). Tập phản xạ: Viết xong 1 hàm nghiệp vụ -> Tạo test ngay.
2. **Quy tắc Single Responsibility (Đơn nhiệm):**
   - Một phương thức chỉ làm **duy nhất một việc**. Hàm ngắn từ 5 - 15 dòng thì cực kỳ dễ test. Hàm dài 200 dòng vừa đọc khó, vừa không thể test nổi!
3. **Tuyệt đối không nhét logic vào Giao diện (Dumb UI):**
   - Nút bấm `JButton` hoặc hàm xử lý API Controller chỉ làm nhiệm vụ nhận input và chuyển tiếp. Logic tính toán phải nằm ở Model hoặc Service.
4. **"Code chưa có test là code chưa hoàn thành":**
   - Đừng vội tự hào khi vừa gõ xong 100 dòng code. Hãy tự hào khi bạn có 5 ca test chứng minh 100 dòng code đó chạy đúng trong mọi tình huống!

---

> 💡 **Khuyến nghị tiếp theo:**
> - [`04-CODE-LEARNING-GUIDE.md`](04-CODE-LEARNING-GUIDE.md) — hiểu luồng đi của dữ liệu qua 3 tầng kiến trúc của dự án.
> - [`02-JDBC.md`](02-JDBC.md) — nắm vững `CallableStatement` và cách quản lý Transaction commit/rollback an toàn.
> - [`05-SQL-JAVA-CONVENTION.md`](05-SQL-JAVA-CONVENTION.md) — bảng tra tên routine và tên cột trả về; rất cần khi viết test cho tầng DAO, vì test đọc kết quả **theo tên cột**.

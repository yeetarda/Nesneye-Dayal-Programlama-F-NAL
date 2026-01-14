package app;

import dao.BookDao;
import dao.LoanDao;
import dao.StudentDao;
import entity.Book;
import entity.Loan;
import entity.Student;

import java.time.LocalDate;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final BookDao bookDao = new BookDao();
    private static final StudentDao studentDao = new StudentDao();
    private static final LoanDao loanDao = new LoanDao();

    public static void main(String[] args) {
        Logger.getLogger("org.hibernate").setLevel(Level.SEVERE);

        System.out.println("Sistem başlatılıyor...");

        while (true) {
            System.out.println("\n--- AKILLI KÜTÜPHANE SİSTEMİ ---");
            System.out.println("1 - Kitap Ekle");
            System.out.println("2 - Kitapları Listele");
            System.out.println("3 - Öğrenci Ekle");
            System.out.println("4 - Öğrencileri Listele");
            System.out.println("5 - Kitap Ödünç Ver");
            System.out.println("6 - Ödünç Listesini Görüntüle");
            System.out.println("7 - Kitap Geri Teslim Al");
            System.out.println("0 - Çıkış");
            System.out.print("Seçiminiz: ");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Lütfen geçerli bir sayı giriniz.");
                continue;
            }

            switch (choice) {
                case 1: addBook(); break;
                case 2: listBooks(); break;
                case 3: addStudent(); break;
                case 4: listStudents(); break;
                case 5: borrowBook(); break;
                case 6: listLoans(); break;
                case 7: returnBook(); break;
                case 0:
                    System.out.println("Çıkış yapılıyor...");
                    System.exit(0);
                    break;
                default: System.out.println("Geçersiz seçim, tekrar deneyin.");
            }
        }
    }

    private static void addBook() {
        System.out.print("Kitap Başlığı (Title): ");
        String title = scanner.nextLine();
        System.out.print("Yazar (Author): ");
        String author = scanner.nextLine();
        System.out.print("Yıl (Year): ");
        int year = Integer.parseInt(scanner.nextLine());

        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setYear(year);
        book.setStatus("AVAILABLE");

        bookDao.save(book);
        System.out.println("Kitap başarıyla eklendi.");
    }

    private static void listBooks() {
        var books = bookDao.getAll();
        System.out.println("\n--- KİTAP LİSTESİ ---");
        if (books.isEmpty()) {
            System.out.println("Sistemde kayıtlı kitap yok.");
        } else {
            for (Book b : books) {
                System.out.println("ID: " + b.getId() + 
                                   " | Başlık: " + b.getTitle() + 
                                   " | Yazar: " + b.getAuthor() + 
                                   " | Durum: " + b.getStatus());
            }
        }
    }

    private static void addStudent() {
        System.out.print("Öğrenci Adı (Name): ");
        String name = scanner.nextLine();
        System.out.print("Bölüm (Department): ");
        String department = scanner.nextLine();

        Student student = new Student();
        student.setName(name);
        student.setDepartment(department);

        studentDao.save(student);
        System.out.println("Öğrenci başarıyla eklendi.");
    }

    private static void listStudents() {
        var students = studentDao.getAll();
        System.out.println("\n--- ÖĞRENCİ LİSTESİ ---");
        if (students.isEmpty()) {
            System.out.println("Sistemde kayıtlı öğrenci yok.");
        } else {
            for (Student s : students) {
                System.out.println("ID: " + s.getId() + 
                                   " | Ad: " + s.getName() + 
                                   " | Bölüm: " + s.getDepartment());
            }
        }
    }

    private static void borrowBook() {
        System.out.print("Öğrenci ID: ");
        int sId = Integer.parseInt(scanner.nextLine());
        System.out.print("Kitap ID: ");
        int kId = Integer.parseInt(scanner.nextLine());

        Book book = bookDao.getById(kId);
        Student student = studentDao.getById(sId);

        if (book == null || student == null) {
            System.out.println("HATA: Girilen ID'ye ait Kitap veya Öğrenci bulunamadı.");
            return;
        }

        if ("BORROWED".equals(book.getStatus())) {
            System.out.println("HATA: Bu kitap şu anda başkasında (BORROWED)!");
            return;
        }

        java.sql.Date date = java.sql.Date.valueOf(LocalDate.now());
        
        Loan loan = new Loan();
        loan.setStudent(student);
        loan.setBook(book);
        loan.setBorrowDate(date);

        book.setStatus("BORROWED");
        bookDao.update(book);

        loanDao.save(loan);
        System.out.println("İşlem başarılı: Kitap ödünç verildi.");
    }

    private static void listLoans() {
        var loans = loanDao.getAll();
        System.out.println("\n--- ÖDÜNÇ LİSTESİ ---");
        if (loans.isEmpty()) {
            System.out.println("Henüz ödünç alma işlemi yapılmamış.");
        } else {
            for (Loan l : loans) {
                System.out.println("Kayıt ID: " + l.getId() +
                        " | Öğrenci: " + l.getStudent().getName() +
                        " | Kitap: " + l.getBook().getTitle() +
                        " | Alış Tarihi: " + l.getBorrowDate() +
                        " | İade Tarihi: " + (l.getReturnDate() == null ? "Teslim Edilmedi" : l.getReturnDate()));
            }
        }
    }

    private static void returnBook() {
        System.out.print("İade Edilecek Kitap ID: ");
        int kId = Integer.parseInt(scanner.nextLine());

        Loan loan = loanDao.getAktifOduncByKitapId(kId);

        if (loan == null) {
            System.out.println("Bu kitap şu an ödünçte görünmüyor veya ID yanlış.");
            return;
        }

        java.sql.Date date = java.sql.Date.valueOf(LocalDate.now());
        loan.setReturnDate(date);
        loanDao.update(loan);

        Book book = loan.getBook();
        book.setStatus("AVAILABLE");
        bookDao.update(book);

        System.out.println("Kitap iade alındı. Durumu 'AVAILABLE' olarak güncellendi.");
    }
}
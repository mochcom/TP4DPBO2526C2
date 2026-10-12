// ============================================================================
// PeopleMenu.java
// Class utama aplikasi PeopleData (tema: Rumah Sakit Psikologi).
// Class ini terhubung dengan PeopleMenu.form (tampilan) lewat "field name".
// Fitur: Create, Read, Update, Delete + konfirmasi hapus + validasi input.
// ============================================================================

// ---------------------------- IMPORT ----------------------------------------
import javax.swing.*;                           // seluruh komponen Swing: JFrame, JPanel, JTable, JButton, JOptionPane, dst.
import javax.swing.table.DefaultTableModel;     // model data JTable, memungkinkan kita menambah baris dan mengganti isi tabel
import java.awt.event.MouseAdapter;             // class kerangka listener mouse, cukup menimpa method yang dibutuhkan
import java.awt.event.MouseEvent;               // objek yang membawa informasi kejadian mouse (klik, posisi, dll.)
import java.time.Year;                          // untuk mengambil tahun sekarang sebagai batas atas Tahun Lahir
import java.util.ArrayList;                     // list dinamis, ukurannya bisa bertambah dan berkurang

public class PeopleMenu {                       // deklarasi class; namanya harus sama dengan nama file dan nama form

    // ========================================================================
    // ATRIBUT KOMPONEN FORM
    // Nama variabel HARUS sama persis dengan "field name" di PeopleMenu.form.
    // Kalau berbeda, muncul error "Cannot bind: field does not exist".
    // ========================================================================
    private JPanel mainPanel;                   // panel utama (root), dipasang ke JFrame di method main()
    private JPanel formPanel;                   // panel penampung label dan isian (wajib ada karena field name-nya terisi)
    private JTextField idField;                 // kotak isian ID
    private JTextField namaField;               // kotak isian Nama
    private JTextField tahunLahirField;         // kotak isian Tahun Lahir (teks yang nanti diubah jadi angka)
    private JComboBox<String> kategoriComboBox; // dropdown Kategori; <String> artinya itemnya berupa teks
    private JComboBox<String> mbtiComboBox;     // dropdown MBTI (atribut baru, komponen bukan JTextField untuk bonus +20)
    private JPanel buttonPanel;                 // panel penampung tiga tombol
    private JButton addUpdateButton;            // tombol dua fungsi: "Add" (tambah) atau "Update" (ubah)
    private JButton deleteButton;               // tombol Delete, hanya tampil saat ada baris terpilih
    private JButton cancelButton;               // tombol Cancel untuk mengosongkan form
    private JScrollPane peopleScrollPane;       // pembungkus tabel agar bisa di-scroll
    private JTable peopleTable;                 // tabel penampil seluruh data Person

    // ========================================================================
    // ATRIBUT TAMBAHAN (bukan dari form)
    // ========================================================================
    private ArrayList<Person> personList;       // "database sementara" yang menyimpan semua objek Person
    private int selectedIndex = -1;             // indeks baris yang dipilih; -1 berarti belum ada yang dipilih (mode Add)

    // ========================================================================
    // KONSTRUKTOR
    // Dijalankan sekali saat objek PeopleMenu dibuat di main().
    // Tugasnya menyiapkan data, mengisi tabel, mengisi dropdown, dan memasang listener.
    // ========================================================================
    public PeopleMenu() {
        personList = new ArrayList<>();         // membuat list kosong; wajib sebelum dipakai agar tidak NullPointerException

        populateList();                         // mengisi list dengan data awal (hardcoded)
        setTable();                             // menampilkan isi list ke JTable

        // mengisi pilihan dropdown Kategori lewat kode; setModel menimpa isi bawaan dari form
        kategoriComboBox.setModel(new DefaultComboBoxModel<>(new String[]{
                "Pasien", "Psikolog", "Psikiater", "Perawat", "Konselor"
        }));

        // mengisi pilihan dropdown MBTI dengan 16 tipe kepribadian
        mbtiComboBox.setModel(new DefaultComboBoxModel<>(new String[]{
                "INTJ", "INTP", "ENTJ", "ENTP", "INFJ", "INFP", "ENFJ", "ENFP",
                "ISTJ", "ISFJ", "ESTJ", "ESFJ", "ISTP", "ISFP", "ESTP", "ESFP"
        }));

        deleteButton.setVisible(false);         // sembunyikan tombol Delete karena belum ada data yang dipilih

        // ---------------- LISTENER tombol Add/Update ----------------
        // "e -> {...}" adalah lambda: kode di dalamnya jalan setiap tombol diklik
        addUpdateButton.addActionListener(e -> {
            if (selectedIndex == -1) {          // belum ada baris terpilih -> user ingin menambah data
                insertData();                   // jalankan proses Create
            } else {                            // ada baris terpilih -> user ingin mengubah data
                updateData();                   // jalankan proses Update
            }
        });

        // ---------------- LISTENER tombol Delete ----------------
        deleteButton.addActionListener(e -> deleteData());  // klik Delete -> jalankan proses hapus (dengan konfirmasi)

        // ---------------- LISTENER tombol Cancel ----------------
        cancelButton.addActionListener(e -> clearForm());   // klik Cancel -> form dikembalikan ke kondisi awal

        // ---------------- LISTENER klik baris tabel ----------------
        // MouseAdapter dibuat sebagai anonymous class, hanya method mouseClicked yang kita isi
        peopleTable.addMouseListener(new MouseAdapter() {
            @Override                                           // menandakan kita menimpa method bawaan MouseAdapter
            public void mouseClicked(MouseEvent e) {            // dipanggil otomatis setiap tabel diklik
                int row = peopleTable.getSelectedRow();         // indeks baris yang diklik (mulai dari 0), -1 jika tidak ada
                if (row == -1) {                                // jika klik tidak mengenai baris data
                    return;                                     // hentikan method, tidak ada yang dikerjakan
                }

                selectedIndex = row;                            // simpan indeks baris terpilih untuk Update/Delete
                Person p = personList.get(selectedIndex);       // ambil objek Person dari list (bukan dari teks tabel)

                idField.setText(p.getId());                                 // tampilkan ID di form
                namaField.setText(p.getNama());                             // tampilkan Nama di form
                tahunLahirField.setText(String.valueOf(p.getTahunLahir())); // int diubah ke String karena setText butuh teks
                kategoriComboBox.setSelectedItem(p.getKategori());          // pilih item dropdown yang sama dengan data
                mbtiComboBox.setSelectedItem(p.getMbti());                  // pilih item dropdown MBTI yang sesuai

                addUpdateButton.setText("Update");              // ubah teks tombol menjadi mode edit
                deleteButton.setVisible(true);                  // munculkan tombol Delete
            }
        });
    }

    // ========================================================================
    // populateList(): mengisi data awal ke personList
    // Urutan parameter Person: id, nama, tahunLahir, kategori, mbti
    // ========================================================================
    private void populateList() {
        personList.add(new Person("P001", "Rina Amelia", 1998, "Pasien", "INFJ"));        // data 1
        personList.add(new Person("P002", "Dr. Budi Santoso", 1980, "Psikiater", "INTJ")); // data 2
        personList.add(new Person("P003", "Sinta Maharani", 1990, "Psikolog", "ENFP"));    // data 3
        personList.add(new Person("P004", "Andi Pratama", 2001, "Pasien", "ISTP"));        // data 4
        personList.add(new Person("P005", "Lestari Dewi", 1993, "Perawat", "ISFJ"));       // data 5
        personList.add(new Person("P006", "Fajar Nugroho", 1987, "Konselor", "ENFJ"));     // data 6
    }

    // ========================================================================
    // setTable(): menggambar ulang tabel berdasarkan isi personList (READ)
    // Dipanggil setiap kali data berubah supaya tampilan selalu sinkron.
    // ========================================================================
    public void setTable() {
        // judul kolom; urutannya harus sama dengan urutan isi rowData di bawah
        String[] columnNames = {"No", "ID", "Nama", "Tahun Lahir", "Kategori", "MBTI"};

        // model tabel baru dengan judul kolom di atas dan 0 baris awal
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);

        int no = 1;                                     // penomoran tampilan, dimulai dari 1

        for (Person person : personList) {              // ambil setiap objek Person di list satu per satu
            Object[] rowData = {                        // satu baris tabel; Object[] karena isinya campuran int dan String
                    no,                                 // kolom No
                    person.getId(),                     // kolom ID
                    person.getNama(),                   // kolom Nama
                    person.getTahunLahir(),             // kolom Tahun Lahir
                    person.getKategori(),               // kolom Kategori
                    person.getMbti()                    // kolom MBTI
            };
            model.addRow(rowData);                      // masukkan baris ke model
            no++;                                       // naikkan nomor untuk baris berikutnya
        }

        peopleTable.setModel(model);                    // pasang model ke tabel sehingga data tampil
    }

    // ========================================================================
    // showError(): helper untuk menampilkan dialog error
    // Dibuat agar pemanggilan dialog yang sama tidak ditulis berulang.
    // ========================================================================
    private void showError(String pesan) {
        // parameter: parent (null = tengah layar), isi pesan, judul dialog, tipe ikon (silang merah)
        JOptionPane.showMessageDialog(null, pesan, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ========================================================================
    // readAndValidate(): membaca isi form lalu memvalidasinya
    // ignoreIndex : baris yang dikecualikan saat cek ID duplikat
    //               (-1 saat Add, selectedIndex saat Update)
    // return      : objek Person valid, atau null jika ada input yang salah
    // ========================================================================
    private Person readAndValidate(int ignoreIndex) {
        String id = idField.getText().trim();                   // ambil ID; trim() membuang spasi di awal/akhir
        String nama = namaField.getText().trim();               // ambil Nama; trim() membuang spasi di awal/akhir
        String tahunText = tahunLahirField.getText().trim();    // ambil Tahun Lahir, masih berupa teks

        // ---------- VALIDASI 1: input tidak boleh kosong ----------
        if (id.isEmpty()) {                                     // isEmpty() true jika teks tidak berisi karakter
            showError("ID tidak boleh kosong!");                // tampilkan dialog error
            return null;                                        // null = validasi gagal, hentikan method
        }
        if (nama.isEmpty()) {                                   // cek Nama
            showError("Nama tidak boleh kosong!");
            return null;
        }
        if (tahunText.isEmpty()) {                              // cek Tahun Lahir
            showError("Tahun Lahir tidak boleh kosong!");
            return null;
        }

        // ---------- VALIDASI 2: ID tidak boleh duplikat ----------
        if (isIdExists(id, ignoreIndex)) {                      // true jika ID sudah dipakai data lain
            showError("ID \"" + id + "\" sudah digunakan! Gunakan ID yang berbeda.");
            return null;
        }

        // ---------- VALIDASI 3: Tahun Lahir harus angka ----------
        int tahunLahir;                                         // dideklarasikan di luar try agar bisa dipakai setelah try-catch
        try {
            tahunLahir = Integer.parseInt(tahunText);           // ubah teks jadi angka; gagal -> NumberFormatException
        } catch (NumberFormatException ex) {                    // dijalankan hanya jika teks bukan angka (mis. "abc", "19.9")
            showError("Tahun Lahir harus berupa angka! Contoh: 1998");
            return null;
        }

        // ---------- VALIDASI 4: Tahun Lahir harus masuk akal ----------
        int tahunSekarang = Year.now().getValue();              // tahun saat program dijalankan
        if (tahunLahir < 1900 || tahunLahir > tahunSekarang) {  // "||" artinya ATAU: salah satu terpenuhi sudah cukup
            showError("Tahun Lahir harus di antara 1900 dan " + tahunSekarang + "!");
            return null;
        }

        // ---------- SEMUA VALID: bentuk objek Person ----------
        String kategori = kategoriComboBox.getSelectedItem().toString();    // item dropdown terpilih (Object -> String)
        String mbti = mbtiComboBox.getSelectedItem().toString();            // dropdown tidak perlu divalidasi, user hanya bisa memilih
        return new Person(id, nama, tahunLahir, kategori, mbti);            // kembalikan objek valid
    }

    // ========================================================================
    // insertData(): CREATE, menambah data baru
    // ========================================================================
    public void insertData() {
        Person baru = readAndValidate(-1);                      // -1 = tidak ada baris yang dikecualikan pada cek duplikat
        if (baru == null) {                                     // null berarti validasi gagal (dialog error sudah tampil)
            return;                                             // batalkan penambahan
        }

        personList.add(baru);                                   // simpan objek baru ke list
        setTable();                                             // refresh tabel agar data baru terlihat
        clearForm();                                            // kosongkan form untuk input berikutnya
        JOptionPane.showMessageDialog(null, "Data berhasil ditambahkan!",
                "Sukses", JOptionPane.INFORMATION_MESSAGE);     // pesan sukses dengan ikon informasi
    }

    // ========================================================================
    // updateData(): UPDATE, mengubah data yang sedang dipilih
    // ========================================================================
    public void updateData() {
        Person hasil = readAndValidate(selectedIndex);          // baris yang diedit dikecualikan dari cek duplikat ID
        if (hasil == null) {                                    // validasi gagal
            return;                                             // batalkan perubahan
        }

        Person p = personList.get(selectedIndex);               // ambil objek lama yang akan diubah
        p.setId(hasil.getId());                                 // timpa ID dengan nilai baru
        p.setNama(hasil.getNama());                             // timpa Nama
        p.setTahunLahir(hasil.getTahunLahir());                 // timpa Tahun Lahir
        p.setKategori(hasil.getKategori());                     // timpa Kategori
        p.setMbti(hasil.getMbti());                             // timpa MBTI

        setTable();                                             // refresh tabel
        clearForm();                                            // kembali ke mode Add
        JOptionPane.showMessageDialog(null, "Data berhasil diubah!",
                "Sukses", JOptionPane.INFORMATION_MESSAGE);
    }

    // ========================================================================
    // deleteData(): DELETE, menghapus data setelah user mengonfirmasi
    // ========================================================================
    public void deleteData() {
        if (selectedIndex == -1) {                              // jaga-jaga jika belum ada baris terpilih
            showError("Pilih data yang ingin dihapus terlebih dahulu!");
            return;
        }

        Person p = personList.get(selectedIndex);               // ambil data yang akan dihapus untuk ditampilkan di pesan

        // dialog konfirmasi; mengembalikan int yang menunjukkan tombol yang ditekan user
        int pilihan = JOptionPane.showConfirmDialog(
                null,                                           // parent: dialog muncul di tengah layar
                "Yakin ingin menghapus data \"" + p.getNama() + "\" (ID: " + p.getId() + ")?",  // isi pertanyaan
                "Konfirmasi Hapus",                             // judul dialog
                JOptionPane.YES_NO_OPTION,                      // tombol yang tampil: Yes dan No
                JOptionPane.WARNING_MESSAGE);                   // ikon peringatan

        if (pilihan == JOptionPane.YES_OPTION) {                // hanya hapus jika user menekan Yes
            personList.remove(selectedIndex);                   // hapus objek pada indeks terpilih
            setTable();                                         // refresh tabel
            clearForm();                                        // reset form dan kembali ke mode Add
            JOptionPane.showMessageDialog(null, "Data berhasil dihapus!",
                    "Sukses", JOptionPane.INFORMATION_MESSAGE);
        }
        // jika user menekan No atau menutup dialog (X), tidak ada yang terjadi dan data tetap aman
    }

    // ========================================================================
    // clearForm(): mengembalikan form ke kondisi awal (mode Add)
    // ========================================================================
    public void clearForm() {
        idField.setText("");                                    // kosongkan isian ID
        namaField.setText("");                                  // kosongkan isian Nama
        tahunLahirField.setText("");                            // kosongkan isian Tahun Lahir
        kategoriComboBox.setSelectedIndex(0);                   // dropdown Kategori kembali ke item pertama
        mbtiComboBox.setSelectedIndex(0);                       // dropdown MBTI kembali ke item pertama
        addUpdateButton.setText("Add");                         // tombol kembali bertugas menambah data
        deleteButton.setVisible(false);                         // sembunyikan tombol Delete
        selectedIndex = -1;                                     // tandai tidak ada baris terpilih
        peopleTable.clearSelection();                           // hilangkan sorotan baris di tabel
    }

    // ========================================================================
    // isIdExists(): mengecek apakah ID sudah dipakai data lain
    // id          : ID yang ingin dicek
    // ignoreIndex : indeks baris yang dilewati (agar data yang sedang diedit tidak dianggap duplikat dirinya sendiri)
    // return      : true jika ID sudah ada, false jika masih aman
    // ========================================================================
    private boolean isIdExists(String id, int ignoreIndex) {
        for (int i = 0; i < personList.size(); i++) {           // periksa seluruh data; memakai indeks i agar tahu posisi baris
            if (i == ignoreIndex) {                             // jika ini baris yang sedang diedit
                continue;                                       // lewati, lanjut ke putaran berikutnya
            }
            // bandingkan ID tanpa membedakan huruf besar/kecil ("p001" dianggap sama dengan "P001")
            if (personList.get(i).getId().equalsIgnoreCase(id.trim())) {
                return true;                                    // ketemu yang sama -> ID sudah terpakai
            }
        }
        return false;                                           // sampai akhir tidak ada yang sama -> ID aman
    }

    // ========================================================================
    // main(): titik awal eksekusi program
    // Agar window muncul, kita bungkus mainPanel ke dalam JFrame.
    // ========================================================================
    public static void main(String[] args) {
        JFrame frame = new JFrame("People Data - RS Psikologi");    // membuat window dengan judul

        PeopleMenu menu = new PeopleMenu();                         // membuat objek menu; konstruktor langsung berjalan

        frame.setContentPane(menu.mainPanel);                       // jadikan mainPanel sebagai isi window
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);       // klik tombol X menghentikan seluruh program
        frame.pack();                                               // ukuran window menyesuaikan isi komponen
        frame.setLocationRelativeTo(null);                          // window muncul di tengah layar
        frame.setVisible(true);                                     // tampilkan window
    }
}
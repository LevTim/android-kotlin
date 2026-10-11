import os
import sys
import tkinter as tk
from tkinter import ttk, filedialog, messagebox, scrolledtext

# ==========================================
# 1. КЛАС КРИПТОСИСТЕМИ (ШИФР ЦЕЗАРЯ)
# ==========================================
class CaesarCipher:
    """
    Клас для реалізації симетричного шифрування шифром Цезаря.
    Підтримує текстовий (укр/англ) та бінарний режими, валідацію та brute-force.
    """
    UA_LOWER = 'абвгґдеєжзиіїйклмнопрстуфхцчшщьюя'
    UA_UPPER = 'АБВГҐДЕЄЖЗИІЇЙКЛМНОПРСТУФХЦЧШЩЬЮЯ'
    EN_LOWER = 'abcdefghijklmnopqrstuvwxyz'
    EN_UPPER = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'

    @staticmethod
    def validate_key(key_str: str) -> tuple[bool, int, str]:
        """Валідація ключа шифрування."""
        if not key_str or not key_str.strip():
            return False, 0, "Ключ не може бути порожнім!"
        try:
            key = int(key_str)
            return True, key, ""
        except ValueError:
            return False, 0, "Ключ повинен бути цілим числом!"

    @staticmethod
    def validate_data(data: str | bytes) -> tuple[bool, str]:
        """Валідація даних для шифрування/розшифрування."""
        if data is None or len(data) == 0:
            return False, "Дані для обробки відсутні або порожні!"
        return True, ""

    @classmethod
    def encrypt_text(cls, text: str, key: int, lang: str = 'ua') -> str:
        """Шифрування текстових даних."""
        if lang == 'ua':
            lower_alphabet, upper_alphabet = cls.UA_LOWER, cls.UA_UPPER
        else:
            lower_alphabet, upper_alphabet = cls.EN_LOWER, cls.EN_UPPER

        n = len(lower_alphabet)
        result = []

        for char in text:
            if char in lower_alphabet:
                idx = (lower_alphabet.index(char) + key) % n
                result.append(lower_alphabet[idx])
            elif char in upper_alphabet:
                idx = (upper_alphabet.index(char) + key) % n
                result.append(upper_alphabet[idx])
            else:
                result.append(char)

        return "".join(result)

    @classmethod
    def decrypt_text(cls, text: str, key: int, lang: str = 'ua') -> str:
        """Розшифрування текстових даних: y = (x - k) mod n."""
        return cls.encrypt_text(text, -key, lang)

    @classmethod
    def brute_force(cls, text: str, lang: str = 'ua') -> list[tuple[int, str]]:
        """Атака методом 'грубої сили' (перебір усіх ключів)."""
        alphabet_len = len(cls.UA_LOWER) if lang == 'ua' else len(cls.EN_LOWER)
        results = []
        for possible_key in range(1, alphabet_len):
            decrypted = cls.decrypt_text(text, possible_key, lang)
            results.append((possible_key, decrypted))
        return results

    @staticmethod
    def process_bytes(data: bytes, key: int, mode: str = 'encrypt') -> bytes:
        """
        Шифрування / розшифрування бінарних файлів будь-якого формату.
        Зсув кожної байтової позиції по модулю 256.
        """
        shift = key % 256 if mode == 'encrypt' else (-key) % 256
        return bytes([(b + shift) % 256 for b in data])


# ==========================================
# 2. ГРАФІЧНИЙ ІНТЕРФЕЙС (GUI)
# ==========================================
class CaesarApp(tk.Tk):
    def __init__(self):
        super().__init__()
        self.title("Криптосистема: Шифр Цезаря — Лабораторна №1")
        self.geometry("800x650")
        self.minsize(700, 500)

        self.current_filepath = None

        self._create_menu()
        self._create_widgets()

    def _create_menu(self):
        """Створення головного меню програми."""
        menubar = tk.Menu(self)

        # Меню "Файл"
        file_menu = tk.Menu(menubar, tearoff=0)
        file_menu.add_command(label="Створити", command=self.new_file, accelerator="Ctrl+N")
        file_menu.add_command(label="Відкрити...", command=self.open_file, accelerator="Ctrl+O")
        file_menu.add_command(label="Зберегти", command=self.save_file, accelerator="Ctrl+S")
        file_menu.add_command(label="Зберегти як...", command=self.save_file_as)
        file_menu.add_separator()
        file_menu.add_command(label="Друк...", command=self.print_file, accelerator="Ctrl+P")
        file_menu.add_separator()
        file_menu.add_command(label="Вихід", command=self.quit_app)
        menubar.add_cascade(label="Файл", menu=file_menu)

        # Меню "Шифрування"
        cipher_menu = tk.Menu(menubar, tearoff=0)
        cipher_menu.add_command(label="Зашифрувати", command=self.encrypt_action)
        cipher_menu.add_command(label="Розшифрувати", command=self.decrypt_action)
        cipher_menu.add_separator()
        cipher_menu.add_command(label="Атака 'Грубої сили' (Brute-Force)", command=self.brute_force_action)
        cipher_menu.add_separator()
        cipher_menu.add_command(label="Шифрувати будь-який файл (Binary Mode)", command=self.process_binary_file)
        menubar.add_cascade(label="Шифрування", menu=cipher_menu)

        # Меню "Довідка"
        help_menu = tk.Menu(menubar, tearoff=0)
        help_menu.add_command(label="Про розробника", command=self.show_about)
        menubar.add_cascade(label="Довідка", menu=help_menu)

        self.config(menu=menubar)

        # Гарячі клавіші
        self.bind("<Control-n>", lambda e: self.new_file())
        self.bind("<Control-o>", lambda e: self.open_file())
        self.bind("<Control-s>", lambda e: self.save_file())
        self.bind("<Control-p>", lambda e: self.print_file())

    def _create_widgets(self):
        """Створення елементів управління та панелей."""
        # Панель інструментів (Toolbar)
        toolbar = ttk.Frame(self, padding=5)
        toolbar.pack(side=tk.TOP, fill=tk.X)

        ttk.Button(toolbar, text="📄 Новий", command=self.new_file).pack(side=tk.LEFT, padx=2)
        ttk.Button(toolbar, text="📂 Відкрити", command=self.open_file).pack(side=tk.LEFT, padx=2)
        ttk.Button(toolbar, text="💾 Зберегти", command=self.save_file).pack(side=tk.LEFT, padx=2)
        ttk.Button(toolbar, text="🖨 Друк", command=self.print_file).pack(side=tk.LEFT, padx=2)
        ttk.Separator(toolbar, orient=tk.VERTICAL).pack(side=tk.LEFT, fill=tk.Y, padx=5)

        ttk.Button(toolbar, text="🔒 Зашифрувати", command=self.encrypt_action).pack(side=tk.LEFT, padx=2)
        ttk.Button(toolbar, text="🔓 Розшифрувати", command=self.decrypt_action).pack(side=tk.LEFT, padx=2)
        ttk.Button(toolbar, text="⚡ Brute-Force", command=self.brute_force_action).pack(side=tk.LEFT, padx=2)

        # Панель налаштувань
        config_frame = ttk.LabelFrame(self, text=" Налаштування криптосистеми ", padding=10)
        config_frame.pack(side=tk.TOP, fill=tk.X, padx=10, pady=5)

        ttk.Label(config_frame, text="Ключ (зсув k):").grid(row=0, column=0, sticky=tk.W, padx=5)
        self.key_entry = ttk.Entry(config_frame, width=15)
        self.key_entry.insert(0, "3")
        self.key_entry.grid(row=0, column=1, sticky=tk.W, padx=5)

        ttk.Label(config_frame, text="Мова алфавіту:").grid(row=0, column=2, sticky=tk.W, padx=15)
        self.lang_var = tk.StringVar(value="ua")
        ttk.Radiobutton(config_frame, text="Українська", variable=self.lang_var, value="ua").grid(row=0, column=3, padx=5)
        ttk.Radiobutton(config_frame, text="English", variable=self.lang_var, value="en").grid(row=0, column=4, padx=5)

        # Текстове поле
        text_frame = ttk.Frame(self, padding=10)
        text_frame.pack(side=tk.TOP, fill=tk.BOTH, expand=True)

        ttk.Label(text_frame, text="Текст для обробки / Результат:").pack(anchor=tk.W, pady=(0, 5))
        self.text_area = scrolledtext.ScrolledText(text_frame, wrap=tk.WORD, font=("Consolas", 11))
        self.text_area.pack(fill=tk.BOTH, expand=True)

        # Рядок стану (Status Bar)
        self.status_bar = ttk.Label(self, text="Готово", relief=tk.SUNKEN, anchor=tk.W, padding=3)
        self.status_bar.pack(side=tk.BOTTOM, fill=tk.X)

    def update_status(self, message: str):
        self.status_bar.config(text=message)

    # --- ФАЙЛОВІ ОПЕРАЦІЇ ---
    def new_file(self):
        self.text_area.delete("1.0", tk.END)
        self.current_filepath = None
        self.update_status("Створено новий документ.")

    def open_file(self):
        filepath = filedialog.askopenfilename(
            filetypes=[("Текстові файли", "*.txt"), ("Усі файли", "*.*")]
        )
        if filepath:
            try:
                with open(filepath, "r", encoding="utf-8") as f:
                    content = f.read()
                self.text_area.delete("1.0", tk.END)
                self.text_area.insert(tk.END, content)
                self.current_filepath = filepath
                self.update_status(f"Відкрито файл: {os.path.basename(filepath)}")
            except Exception as e:
                messagebox.showerror("Помилка", f"Не вдалося відкрити файл:\n{e}")

    def save_file(self):
        if self.current_filepath:
            try:
                with open(self.current_filepath, "w", encoding="utf-8") as f:
                    f.write(self.text_area.get("1.0", tk.END).rstrip("\n"))
                self.update_status(f"Збережено: {os.path.basename(self.current_filepath)}")
            except Exception as e:
                messagebox.showerror("Помилка", f"Не вдалося зберегти файл:\n{e}")
        else:
            self.save_file_as()

    def save_file_as(self):
        filepath = filedialog.asksaveasfilename(
            defaultextension=".txt",
            filetypes=[("Текстові файли", "*.txt"), ("Усі файли", "*.*")]
        )
        if filepath:
            self.current_filepath = filepath
            self.save_file()

    def print_file(self):
        """Імітація / виведення діалогового вікна друку."""
        content = self.text_area.get("1.0", tk.END).strip()
        is_valid, err = CaesarCipher.validate_data(content)
        if not is_valid:
            messagebox.showwarning("Друк", "Немає вмісту для друку!")
            return

        print_window = tk.Toplevel(self)
        print_window.title("Попередній перегляд та друк")
        print_window.geometry("500x400")

        ttk.Label(print_window, text="Надіслано на системний принтер:", font=("Arial", 10, "bold")).pack(pady=10)
        preview = scrolledtext.ScrolledText(print_window, wrap=tk.WORD)
        preview.insert(tk.END, content)
        preview.config(state=tk.DISABLED)
        preview.pack(fill=tk.BOTH, expand=True, padx=10, pady=5)

        ttk.Button(print_window, text="Друкувати", command=lambda: [
            messagebox.showinfo("Друк", "Документ успішно надіслано на принтер!"),
            print_window.destroy()
        ]).pack(pady=10)

        self.update_status("Документ надіслано на друк.")

    def quit_app(self):
        self.destroy()

    # --- КРИПТОГРАФІЧНІ ОПЕРАЦІЇ ---
    def _get_key_and_data(self) -> tuple[bool, int, str, str]:
        key_str = self.key_entry.get()
        valid_key, key, err_key = CaesarCipher.validate_key(key_str)
        if not valid_key:
            messagebox.showerror("Помилка ключа", err_key)
            return False, 0, "", ""

        text = self.text_area.get("1.0", tk.END).rstrip("\n")
        valid_data, err_data = CaesarCipher.validate_data(text)
        if not valid_data:
            messagebox.showerror("Помилка даних", err_data)
            return False, 0, "", ""

        return True, key, text, self.lang_var.get()

    def encrypt_action(self):
        valid, key, text, lang = self._get_key_and_data()
        if not valid:
            return
        encrypted = CaesarCipher.encrypt_text(text, key, lang)
        self.text_area.delete("1.0", tk.END)
        self.text_area.insert(tk.END, encrypted)
        self.update_status(f"Текст успішно зашифровано (Ключ = {key}, Мова = {lang.upper()}).")

    def decrypt_action(self):
        valid, key, text, lang = self._get_key_and_data()
        if not valid:
            return
        decrypted = CaesarCipher.decrypt_text(text, key, lang)
        self.text_area.delete("1.0", tk.END)
        self.text_area.insert(tk.END, decrypted)
        self.update_status(f"Текст успішно розшифровано (Ключ = {key}, Мова = {lang.upper()}).")

    def brute_force_action(self):
        text = self.text_area.get("1.0", tk.END).rstrip("\n")
        valid_data, err_data = CaesarCipher.validate_data(text)
        if not valid_data:
            messagebox.showerror("Помилка даних", err_data)
            return

        lang = self.lang_var.get()
        results = CaesarCipher.brute_force(text, lang)

        # Вікно з результатами перебору
        bf_window = tk.Toplevel(self)
        bf_window.title("Результати атаки Brute-Force")
        bf_window.geometry("650x450")

        ttk.Label(bf_window, text=f"Варіанти розшифрування (Мова: {lang.upper()}):", font=("Arial", 10, "bold")).pack(pady=5)

        bf_text = scrolledtext.ScrolledText(bf_window, wrap=tk.WORD, font=("Consolas", 10))
        bf_text.pack(fill=tk.BOTH, expand=True, padx=10, pady=5)

        for k, dec_text in results:
            bf_text.insert(tk.END, f"=== [ Ключ k = {k:2d} ] ===\n{dec_text}\n\n")

        bf_text.config(state=tk.DISABLED)
        self.update_status("Виконано атака методом 'грубої сили'.")

    def process_binary_file(self):
        """Шифрування/розшифрування будь-яких файлів (зображення, pdf, zip тощо)."""
        key_str = self.key_entry.get()
        valid_key, key, err_key = CaesarCipher.validate_key(key_str)
        if not valid_key:
            messagebox.showerror("Помилка ключа", err_key)
            return

        in_file = filedialog.askopenfilename(title="Оберіть файл для шифрування/розшифрування")
        if not in_file:
            return

        out_file = filedialog.asksaveasfilename(title="Оберіть куди зберегти результат")
        if not out_file:
            return

        try:
            with open(in_file, "rb") as f_in:
                data = f_in.read()

            # Питаємо дію у користувача
            action = messagebox.askyesnocancel("Режим обробки", "Натисніть 'Так' для шифрування, 'Ні' для розшифрування.")
            if action is None:
                return

            mode = 'encrypt' if action else 'decrypt'
            processed_data = CaesarCipher.process_bytes(data, key, mode)

            with open(out_file, "wb") as f_out:
                f_out.write(processed_data)

            messagebox.showinfo("Успіх", f"Файл успішно оброблено ({mode}) та збережено!")
            self.update_status(f"Бінарний файл збережено: {os.path.basename(out_file)}")
        except Exception as e:
            messagebox.showerror("Помилка", f"Помилка під час обробки файлу:\n{e}")

    # --- ДОВІДКА ---
    def show_about(self):
        """Виведення відомостей про розробника."""
        about_text = (
            "Лабораторна робота №1\n"
            "Дисципліна: Безпека інформаційних систем\n"
            "Тема: Шифр Цезаря\n\n"
            "Розробник: Студент групи ТВ-31\n"
            "ЛЕВУН Тимофій Олексійович\n\n"
            "Програма реалізує симетричне шифрування Цезаря з підтримкою\n"
            "української/англійської мов, бінарних файлів та brute-force атаки."
        )
        messagebox.showinfo("Про розробника", about_text)


if __name__ == "__main__":
    app = CaesarApp()
    app.mainloop()
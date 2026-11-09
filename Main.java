import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;
import java.util.Scanner;
import java.util.Locale;
public class Main {
    static class Note {
        int id;
        String title;
        String text;
        Note(int newId, String newTitle, String newText) {
            id = newId;
            title = newTitle;
            text = newText;
        }
    }
    static class Change {
        int noteId;
        String before;
        String after;
        Change(int id, String oldText, String newText) {
            noteId = id;
            before = oldText;
            after = newText;
        }
    }
    static ArrayList<Note> notes = new ArrayList<Note>();
    static int nextId = 1;
    static Stack<Change> undoStack = new Stack<Change>();
    static Stack<Change> redoStack = new Stack<Change>();
    static class TrieNode {
        HashMap<Character, TrieNode> children = new HashMap<Character, TrieNode>();
        ArrayList<Integer> ids = new ArrayList<Integer>();
    }
    static TrieNode root = new TrieNode();
    static HashMap<String, HashSet<Integer>> wordIndex =
            new HashMap<String, HashSet<Integer>>();
    public static void main(String[] args) {
        addNote("Java Stack", "java stack undo redo");
        addNote("Java Queue", "java queue fifo");
        addNote("Math", "matrix algebra");
        addNote("Market", "milk bread");
        Scanner scanner = new Scanner(System.in);
        boolean running = true;
        while (running) {
            System.out.println("\n===== Simplenote Console =====");
            System.out.println("1. ดูโน้ตทั้งหมด");
            System.out.println("2. เพิ่มโน้ต");
            System.out.println("3. แก้เนื้อหาโน้ต (คนที่ 1)");
            System.out.println("4. Undo — ย้อนการแก้ไขล่าสุด (คนที่ 1)");
            System.out.println("5. Redo — ทำการแก้ไขซ้ำ (คนที่ 1)");
            System.out.println("6. ค้นชื่อขึ้นต้นด้วย Trie (คนที่ 2)");
            System.out.println("7. ค้นทุกคำแบบ AND (คนที่ 3)");
            System.out.println("0. ออกจากโปรแกรม");
            System.out.println("Undo: " + undoStack.size() + " | Redo: " + redoStack.size());
            int choice = readNumber(scanner, "เลือกเมนู: ");
            switch (choice) {
                case 1:
                    showAllNotes();
                    break;
                case 2:
                    String title = readText(scanner, "ชื่อโน้ต: ");
                    String text = readText(scanner, "เนื้อหา (บรรทัดเดียว): ");
                    addNote(title, text);
                    System.out.println("เพิ่มโน้ตแล้ว");
                    break;
                case 3:
                    showAllNotes();
                    int id = readNumber(scanner, "id ที่ต้องการแก้ไข: ");
                    Note note = findNote(id);
                    if (note == null) {
                        System.out.println("ไม่พบ id นี้");
                    } else {
                        String newText = readText(scanner, "เนื้อหาใหม่: ");
                        editNote(id, newText);
                        System.out.println("แก้ไขแล้ว");
                    }
                    break;
                case 4:
                    if (undo()) System.out.println("ย้อนการแก้ไขแล้ว");
                    else System.out.println("ไม่มีประวัติให้ย้อนกลับ");
                    break;
                case 5:
                    if (redo()) System.out.println("ทำซ้ำแล้ว");
                    else System.out.println("ไม่มีประวัติให้ทำซ้ำ");
                    break;
                case 6:
                    String prefix = readText(scanner, "ต้นชื่อ เช่น ja หรือ ma: ");
                    showResults(searchPrefix(prefix));
                    break;
                case 7:
                    String query = readText(scanner, "คำค้น เช่น java stack: ");
                    for (String term : splitWords(query)) {
                        if (!term.isEmpty()) {
                            HashSet<Integer> ids = wordIndex.get(term);
                            System.out.println(term + " -> " + (ids == null ? "[]" : ids));
                        }
                    }
                    showResults(searchAnd(query));
                    break;
                case 0:
                    running = false;
                    System.out.println("จบโปรแกรม ข้อมูลตัวอย่างนี้เก็บในหน่วยความจำเท่านั้น");
                    break;
                default:
                    System.out.println("กรุณาเลือก 0 ถึง 7");
            }
        }
        scanner.close();
    }
    static void editNote(int id, String newText) {
        Note note = findNote(id);
        if (note == null || note.text.equals(newText)) return;
        Change change = new Change(id, note.text, newText);
        undoStack.push(change);
        redoStack.clear();
        note.text = newText;
        rebuildIndexes();
    }
    static boolean undo() {
        if (undoStack.empty()) return false;
        Change change = undoStack.pop();
        findNote(change.noteId).text = change.before;
        redoStack.push(change);
        rebuildIndexes();
        return true;
    }
    static boolean redo() {
        if (redoStack.empty()) return false;
        Change change = redoStack.pop();
        findNote(change.noteId).text = change.after;
        undoStack.push(change);
        rebuildIndexes();
        return true;
    }
    static void buildTrie() {
        root = new TrieNode();
        for (Note note : notes) {
            TrieNode current = root;
            current.ids.add(note.id);
            String name = note.title.toLowerCase(Locale.ROOT);
            for (int i = 0; i < name.length(); i++) {
                char letter = name.charAt(i);
                TrieNode next = current.children.get(letter);
                if (next == null) {
                    next = new TrieNode();
                    current.children.put(letter, next);
                }
                current = next;
                current.ids.add(note.id);
            }
        }
    }
    static ArrayList<Integer> searchPrefix(String prefix) {
        TrieNode current = root;
        String name = prefix.toLowerCase(Locale.ROOT);
        for (int i = 0; i < name.length(); i++) {
            current = current.children.get(name.charAt(i));
            if (current == null) return new ArrayList<Integer>();
        }
        return new ArrayList<Integer>(current.ids);
    }
    static String[] splitWords(String text) {
        return text.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{M}\\p{N}]+");
    }
    static void buildWordIndex() {
        wordIndex.clear();
        for (Note note : notes) {
            String[] words = splitWords(note.title + " " + note.text);
            for (String word : words) {
                if (word.isEmpty()) continue;
                HashSet<Integer> ids = wordIndex.get(word);
                if (ids == null) {
                    ids = new HashSet<Integer>();
                    wordIndex.put(word, ids);
                }
                ids.add(note.id);
            }
        }
    }
    static ArrayList<Integer> searchAnd(String query) {
        HashSet<Integer> result = null;
        for (String word : splitWords(query)) {
            if (word.isEmpty()) continue;
            HashSet<Integer> found = wordIndex.get(word);
            if (found == null) return new ArrayList<Integer>();
            if (result == null) {
                result = new HashSet<Integer>(found);
            } else {
                HashSet<Integer> common = new HashSet<Integer>();
                for (int id : result) {
                    if (found.contains(id)) common.add(id);
                }
                result = common;
            }
        }
        if (result == null) return new ArrayList<Integer>();
        return new ArrayList<Integer>(result);
    }
    static void addNote(String title, String text) {
        notes.add(new Note(nextId, title, text));
        nextId++;
        redoStack.clear();
        rebuildIndexes();
    }
    static void rebuildIndexes() {
        buildTrie();
        buildWordIndex();
    }
    static Note findNote(int id) {
        for (Note note : notes) {
            if (note.id == id) return note;
        }
        return null;
    }
    static void showNote(Note note) {
        System.out.println("[" + note.id + "] " + note.title + " | " + note.text);
    }
    static void showAllNotes() {
        for (Note note : notes) showNote(note);
    }
    static void showResults(ArrayList<Integer> ids) {
        if (ids.isEmpty()) System.out.println("ไม่พบโน้ต");
        for (Note note : notes) {
            if (ids.contains(note.id)) showNote(note);
        }
    }
    static int readNumber(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("กรุณาพิมพ์เลขจำนวนเต็ม");
            }
        }
    }
    static String readText(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String text = scanner.nextLine().trim();
            if (!text.isEmpty()) return text;
            System.out.println("กรุณาใส่ข้อความ");
        }
    }
}

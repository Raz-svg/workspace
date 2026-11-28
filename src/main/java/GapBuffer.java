import java.util.ArrayList;
import java.util.Collections;

public class GapBuffer {

    private ArrayList<Character> buffer;
    private int gapstart;
    private int gapend;

    public GapBuffer(){
        buffer = new ArrayList<>();
        gapstart=0;
        gapend=10;
        for(int i =gapstart ;i < gapend ;i++){
            buffer.add('\0');
        }
    }

    public GapBuffer(String text){
        this();
        insert(text);
    }

    public void insert(char c){
        if(gapstart == gapend){
            expandGap();
        }
        buffer.set(gapstart,c);
        gapstart++;
    }

    public void insert(String text){
        if(text == null){
            return;
        }
        for(int i = 0; i < text.length(); i++){
            insert(text.charAt(i));
        }
    }

    private void expandGap(){

        int newgapsize = Math.max(10,buffer.size());
        buffer.addAll(gapend, Collections.nCopies(newgapsize, '\0'));
        gapend =  gapend + newgapsize;
    }

    public void delete(){
        if(gapstart > 0){
            gapstart--;
            buffer.set(gapstart,'\0');
        }
    }

    public void deleteForward(){
        if(gapend < buffer.size()){
            buffer.set(gapend, '\0');
            gapend++;
        }
    }

    public void deleteRange(int start, int end){
        checkRange(start, end);
        moveCursor(end);
        for(int i = 0; i < end - start; i++){
            delete();
        }
    }

    public void left(){
        if(gapstart > 0){
            gapstart--;
            gapend--;
            buffer.set(gapend,buffer.get(gapstart));
            buffer.set(gapstart,'\0');
        }
    }

    public void right(){
        if(gapend<buffer.size()){
            buffer.set(gapstart,buffer.get(gapend));
            buffer.set(gapend,'\0');
            gapstart++;
            gapend++;
        }
    }

    public void moveCursor(int pos){
        if(pos < 0 || pos > length()){
            throw new IndexOutOfBoundsException("Cursor position out of bounds: " + pos);
        }
        while(gapstart > pos){
            left();
        }
        while(gapstart < pos){
            right();
        }
    }

    public int length(){
        return buffer.size() - (gapend - gapstart);
    }

    public int getCursorPosition(){
        return gapstart;
    }

    public char charAt(int index){
        if(index < 0 || index >= length()){
            throw new IndexOutOfBoundsException("Index out of bounds: " + index);
        }
        if(index < gapstart){
            return buffer.get(index);
        }
        return buffer.get(index + (gapend - gapstart));
    }

    public String substring(int start, int end){
        checkRange(start, end);
        StringBuilder sb = new StringBuilder(end - start);
        for(int i = start; i < end; i++){
            sb.append(charAt(i));
        }
        return sb.toString();
    }

    public String getText(){
        StringBuilder sb = new StringBuilder(length());
        for(int i = 0; i < gapstart; i++){
            sb.append(buffer.get(i));
        }
        for(int i = gapend; i < buffer.size(); i++){
            sb.append(buffer.get(i));
        }
        return sb.toString();
    }

    @Override
    public String toString(){
        return getText();
    }

    private void checkRange(int start, int end){
        if(start < 0 || end > length() || start > end){
            throw new IndexOutOfBoundsException("Invalid range: [" + start + ", " + end + ")");
        }
    }



    
}

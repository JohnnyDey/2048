import java.util.*;

public class Model {
    private static final int FIELD_WIDTH = 4;
    private Tile[][] gameTiles;
    protected int score, maxTile;
    private Stack<Tile[][]> previousStates = new Stack<>();
    private Stack<Integer> previousScores = new Stack<>();
    private boolean isSaveNeeded = true;

    public Model() {
        resetGameTiles();
        score = 0;
        maxTile = 2;
    }
    private void saveState(Tile[][] tiles){
        Tile[][] saveState = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for(int i = 0; i < tiles.length; i++){
            for(int j = 0; j < tiles[0].length; j++){
                saveState[i][j] = new Tile(gameTiles[i][j].value);
            }
        }
        int saveScore = score;
        previousStates.push(saveState);
        previousScores.push(saveScore);
        isSaveNeeded = false;
    }
    public void rollback(){
        if(previousScores.size() > 0 && previousStates.size() > 0) {
            gameTiles = previousStates.pop();
            score =  previousScores.pop();
        }
    }
    private boolean hasBoardChanged(){
        int sum1 = 0;
        int sum2 = 0;
        if(!previousStates.isEmpty()) {
            Tile[][] prevGameTiles = previousStates.peek();
            for (int i = 0; i < FIELD_WIDTH; i++) {
                for (int j = 0; j < FIELD_WIDTH; j++) {
                    sum1 += gameTiles[i][j].value;
                    sum2 += prevGameTiles[i][j].value;
                }
            }
        }
        return sum1 != sum2;
    }
    public void autoMove(){
        PriorityQueue<MoveEfficiency> priorityQueue =  new PriorityQueue<>(4, Collections.reverseOrder());
        priorityQueue.add(getMoveEfficiency(this::left));
        priorityQueue.add(getMoveEfficiency(this::right));
        priorityQueue.add(getMoveEfficiency(this::down));
        priorityQueue.add(getMoveEfficiency(this::up));
        priorityQueue.peek().getMove().move();
    }
    private MoveEfficiency getMoveEfficiency(Move move){
        move.move();
        MoveEfficiency moveEfficiency;
        int emptyCount = 0;
        for(int i = 0; i < gameTiles.length; i++){
            for(int j = 0; j < gameTiles[0].length; j++){
                if(gameTiles[i][j].value == 0) emptyCount++;
            }
        }
        if(hasBoardChanged())
            moveEfficiency = new MoveEfficiency(emptyCount, score, move);
        else
            moveEfficiency = new MoveEfficiency(-1, 0, move);
        rollback();
        return moveEfficiency;
    }
    public void randomMove(){
        int n = ((int) (Math.random() * 100)) % 4;
        if(n == 0) left();
        if(n == 1) up();
        if(n == 2) right();
        if(n == 3) down();
    }
    private void addTile(){
        List<Tile> tile = getEmptyTiles();
        if(!tile.isEmpty())
            tile.get((int)(tile.size() * Math.random())).value = (Math.random() < 0.9 ? 2 : 4);
    }
    private List<Tile> getEmptyTiles(){
        List list = new ArrayList();
        for(int i = 0; i < FIELD_WIDTH; i++){
            for(int j = 0; j < FIELD_WIDTH; j++){
                if(gameTiles[i][j].isEmpty()) list.add(gameTiles[i][j]);
            }
        }
        return list;
    }
    protected void resetGameTiles(){
        gameTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for(int i = 0; i < FIELD_WIDTH; i++){
            for(int j = 0; j < FIELD_WIDTH; j++){
                gameTiles[i][j] = new Tile();
            }
        }
        addTile();
        addTile();
    }
    private boolean compressTiles(Tile[] tiles){
        List<Tile> list = Arrays.asList(tiles);
        ArrayList<Tile> tilesList = new ArrayList<>(list);
        int counter=0;
        for (int j = 0; j < tilesList.size();) {
            if(counter>tilesList.size()) break;
            if(tilesList.get(j).value == 0) {
                tilesList.add(tilesList.get(j));
                tilesList.remove(j);
                counter++;
                continue;
            }
            counter++;
            j++;
        }
        boolean isChanged = false;
        for (int i = 0; i < tilesList.size(); i++) {
            if(tilesList.get(i).value != tiles[i].value) {
                isChanged = true;
            }
        }
        if(isChanged) {
            tilesList.toArray(tiles);
            return true;
        }
        else return false;
    }
    private boolean mergeTiles(Tile[] tiles){
        boolean isChanged = false;
        for (int j = 0; j < tiles.length-1; j++) {
            if(tiles[j].value == tiles[j+1].value && tiles[j].value!=0) {
                isChanged = true;
                tiles[j].value *= 2;
                if(tiles[j].value > maxTile) {
                    maxTile=tiles[j].value;
                }
                score += tiles[j].value;
                tiles[j+1].value=0;
                compressTiles(tiles);
            }
        }
        return isChanged;
    }
    public static void main(String... a){
        Tile[] tiles = {new Tile(4), new Tile(0), new Tile(0), new Tile(0)};
        System.out.print(new Model().mergeTiles(tiles));
    }
    void left(){
        if(isSaveNeeded) saveState(gameTiles);
        boolean isChanged = false;
        for (int i = 0; i < FIELD_WIDTH; i++) {
            boolean compress = compressTiles(gameTiles[i]);
            boolean merge = mergeTiles(gameTiles[i]);
            if(compress || merge) {
                isChanged=true;
            }
        }
        if (isChanged) addTile();
        isSaveNeeded = true;
    }
    void up(){
        saveState(gameTiles);
        rotate();
        left();
        rotate();
        rotate();
        rotate();
    }
    void down(){
        saveState(gameTiles);
        rotate();
        rotate();
        rotate();
        left();
        rotate();

    }
    void right(){
        saveState(gameTiles);
        rotate();
        rotate();
        left();
        rotate();
        rotate();
    }
    private void rotate(){
        Tile[][] newTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for(int i = 0; i < FIELD_WIDTH; i++){
            for(int j = 0; j < FIELD_WIDTH; j++){
                newTiles[FIELD_WIDTH-j-1][i] = gameTiles[i][j];
            }
        }
        gameTiles = newTiles;
    }

    public Tile[][] getGameTiles() {
        return gameTiles;
    }
    public boolean canMove(){
        Tile[][] saveState = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for(int i = 0; i < gameTiles.length; i++){
            for(int j = 0; j < gameTiles[0].length; j++){
                saveState[i][j] = gameTiles[i][j];
            }
        }
        if(isPossible() || isPossible() || isPossible() || isPossible()){
            gameTiles = saveState;
            return true;
        }
        gameTiles = saveState;
        return false;
    }
    private boolean isPossible(){
        for (int i = 0; i < FIELD_WIDTH; i++) {
            if(compressTiles(gameTiles[i]) || mergeTiles(gameTiles[i])) {
                return true;
            }
        }
        rotate();
        return false;
    }
}

package de.uni_postdam.hpi.matrix;

import java.io.PrintStream;


public class Matrix{

	private int cols;
	private int rows;
	
	private boolean isEmpty = true;
	
	private int[][] content = null;
	
	protected int default_value(){
		return -1; 
	}
	
	
	public Matrix(int cols, int rows) {
		this.cols = cols;
		this.rows = rows;
		this.content = new int[cols][rows];
		int default_val = default_value();
		for(int row = 0; row < rows; row++){
			for(int col = 0; col < cols; col++){
				this.content[col][row] = default_val;
			}
		}
	}

	public Matrix(int cols, int rows, int[] content) {
		this(cols, rows);
		this.setContent(content);
	}

	
	public void setContent(int[] content){
		if(cols*rows != content.length){
			throw new IllegalArgumentException(
					String.format("content does not math the dimensions: cols=%d, rows=%d and content length: %d!",
							cols, rows, content.length));
		}
		this.isEmpty = false;
		for(int col = 0; col < cols; col++){
			for(int row = 0; row < rows; row++){
				this.content[col][row] = content[col + row * cols];
			}
		}
		
	}
	
	public int cols() {
		return cols;
	}

	public int rows() {
		return rows;
	}

	public int size() {
		return rows*cols;
	}

	public boolean isEmpty() {
		return isEmpty;
	}

	public int get(int col, int row) {
		if(this.isEmpty) return 0;
		else{
			return this.content[col][row];
		}
	}
	
	public void set(int col, int row, int value){
		if(this.isEmpty) this.isEmpty = false;
		this.content[col][row] = value;
	}
	
	public void rangeSet(int beginCol, int beginRow, Matrix other) {
		if(beginCol + other.cols > this.cols || beginRow + other.rows > this.rows){
			throw new IllegalArgumentException("Destination matrix is too small!");
		}

		int col = beginCol;
		for(int otherCol = 0; otherCol < other.cols; otherCol++){
			int row = beginRow;
			for(int otherRow = 0; otherRow < other.rows; otherRow++){
				this.set(col, row, other.get(otherCol, otherRow));
				row++;
			}
			col++;
		}
	}
	

	public Matrix rangeGet(int beginCol, int beginRow, int cols, int rows) {
		if(beginCol + cols > this.cols || beginRow + rows > this.rows){
			throw new IllegalArgumentException("Source matrix is too small!");
		}
		
		Matrix result = new Matrix(cols, rows);
		for(int col = 0; col < cols; col++){
			for(int row = 0; row < rows; row++){
				result.set(col, row, this.get(col + beginCol, row + beginRow));
			}
		}
		return result;
	}
	
	
	public void print(PrintStream stream){
		stream.print(this.toString());
	}
	
	
	public void setWithIdx(int idx, int value){
		this.set(idx % cols(), idx / cols(), value);
	}
	
	public int getWithIdx(int idx){
		return this.get(this.colFromIdx(idx), this.rowFromIdx(idx));
	}
	
	private int colFromIdx(int idx){
		return idx % cols();
	}
	
	private int rowFromIdx(int idx){
		return idx / cols();
	}
	
	
	@Override
	public boolean equals(Object obj) {
		Matrix other = null;
		if (obj instanceof Matrix) {
			other = (Matrix) obj;
		}else {
			return false;
		}
		if(other.cols() != this.cols() || other.rows() != this.rows()){
			return false;
		}
		for(int col = 0; col < this.cols; col++){
			for(int row = 0; row < this.rows; row++){
				if(this.get(col, row) != other.get(col, row)){
					return false;
				}
			}
		}
		return true;
	}
	
	
	@Override
	public String toString() {
		String result = "";
		for(int row = 0; row < rows; row++){
			for(int col = 0; col < cols; col++){
				result += String.format("%d%s", this.get(col, row), this.delimiter());
			}
			result += "\n";
		}
		return result;
	}

	protected String delimiter(){
		return "\t";
	}



	
	
}

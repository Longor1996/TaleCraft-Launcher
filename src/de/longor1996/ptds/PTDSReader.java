package de.longor1996.ptds;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Stack;

import de.longor1996.collections.StringQueue;

/**
 * 
 * PlainTextDataStructure-Document reader, using an InputStream.
 * 
 * The PTDS Format is a simple key-block/element/array format.
 * 
 * String Literal Key: "AnyASCIIText"
 * String Literal Value: "AnyASCIIText"
 * 
 * Types:
 * Block: KEY {}
 * Element: KEY VALUE
 * Array: KEY [VALUE,VALUE,VALUE,...]
 * 
 * Its not really recommended using this format! Use JSON instead!
 * 
 **/
public class PTDSReader implements Closeable{
	// is in debug mode?
	public static boolean isInDebugMode = false;
	
	// the inputStream the PTDS is constructed from
	private InputStream input;
	
	// state machine stuff
	private boolean isInCommentBlock;
	private boolean isInCommentLine;
	private boolean isInStringLiteral;
	private boolean isInArray;
	
	// current line/row position
	private int currentLineNumber;
	private int currentRowNumber;
	
	// string literal variables
	private String lastCompletedStringLiteral = null;
	private StringBuffer stringLiteralBuffer = null;
	private StringQueue stringLiteralQueue = null;
	
	// array string literal variables
	private ArrayList<String> arrayLiteralBuffer;
	private String arrayNameLiteral;
	
	// PTDS block stack
	private Stack<PTDSBlock> blockStack;
	
	public PTDSReader(InputStream inputStream) {
		// save the inputStream reference
		this.input = inputStream;
		
		// create the blockStack
		this.blockStack = new Stack<PTDSBlock>();
		
		// and create the document block
		this.blockStack.add(new PTDSBlock("document"));
		
		// and create the string literal queue
		this.stringLiteralQueue = new StringQueue(4);
	}
	
	/**
	 * Returns the resulting document.
	 * If the read() method wasn't called yet, the returned element will contain no data.
	 * @return the resulting PTDS document.
	 **/
	public PTDSBlock getDocument(){
		return this.blockStack.peek();
	}
	
	/** Reads the inputStream that was given trough the constructor and creates a PTDS-structure from it. **/
	public void read() throws Exception{
		// char variables
		int iChar = ' ';
		char cChar = ' ';
		char cLastChar = ' ';
		
		// read characters
		while((iChar = this.input.read()) != -1){
			cChar = (char) iChar;
			
			this.parseChar(cLastChar,cChar);
			
			if(iChar == '\n'){
				this.currentLineNumber++;
				this.currentRowNumber = 0;
			} else
				this.currentRowNumber++;
			
			cLastChar = cChar;
		}
		
	}
	
	/** Closes the inputStream the parser uses. **/
	@Override
	public void close() throws IOException {
		// close stream
		this.input.close();
	}
	
	/** Most complex method. Does all the parsing of the stream-data. **/
	private void parseChar(char cLastChar, char cChar) {
		
		// \n check!
		if(cChar == '\n'){
			
			if(isInDebugMode)
				System.out.println("[?] line feed!");
			
			// line-comment end!
			this.isInCommentLine = false;
			return;
		}
		
		// line-comment start check!
		if(!this.isInStringLiteral && (cLastChar == '\\') && (cChar == '\\')){
			
			if(isInDebugMode)
				System.out.println("[?] comment line start!");
			this.isInCommentLine = true;
			return;
		}
		
		// Block-comment start check!
		if(!this.isInStringLiteral && (cLastChar == '\\') && (cChar == '*')){

			if(isInDebugMode)
				System.out.println("[?] comment block start!");
			this.isInCommentBlock = true;
			return;
		}
		// Block-comment end check!
		if(!this.isInStringLiteral && (cLastChar == '*') && (cChar == '\\')){

			if(isInDebugMode)
				System.out.println("[?] comment block end!");
			this.isInCommentBlock = false;
			return;
		}
		
		
		// Comment case-return check!
		if(this.isInCommentLine)
			return;
		if(this.isInCommentBlock)
			return;
		
		// String literal check!
		// String Literal start check!
		if(!this.isInStringLiteral && (cLastChar != '\\') && (cChar == '"')){

			if(isInDebugMode)
				System.out.println("[?] started new string literal!");
			this.isInStringLiteral = true;
			this.stringLiteralBuffer = new StringBuffer("");
			return;
		}
		// String Literal end check!
		if(this.isInStringLiteral && (cLastChar != '\\') && (cChar == '"')){
			this.isInStringLiteral = false;
			this.lastCompletedStringLiteral = this.stringLiteralBuffer.toString();
			this.stringLiteralQueue.push(this.lastCompletedStringLiteral);
			

			if(isInDebugMode)
				System.out.println("[?] completed string literal: " + this.lastCompletedStringLiteral + " ("+this.stringLiteralBuffer.toString()+")");
			this.stringLiteralBuffer = new StringBuffer("");
			return;
		}
		
		// Block start check!
		if(!this.isInStringLiteral && (cChar == '{')){
			
			if(this.lastCompletedStringLiteral != null){
				PTDSBlock newBlock = new PTDSBlock(this.blockStack.peek(),this.lastCompletedStringLiteral);
				this.blockStack.push(newBlock);
			} else
				throw new IllegalArgumentException("Can't create new block: No string literal set! @" + this.currentLineNumber + ":" + this.currentRowNumber);
			
			return;
		}
		// Block end check!
		if(!this.isInStringLiteral && (cChar == '}')){
			this.blockStack.pop();
			return;
		}
		
		
		// Array start check!
		if(!this.isInStringLiteral && !this.isInArray && (cChar == '[')){
			this.arrayLiteralBuffer = new ArrayList<String>();
			
			if(this.lastCompletedStringLiteral != null){
				this.arrayNameLiteral = this.lastCompletedStringLiteral;
				this.lastCompletedStringLiteral = null;
			} else
				throw new IllegalArgumentException("Can't create array: No string literal set! @" + this.currentLineNumber + ":" + this.currentRowNumber);
			
			this.isInArray = true;

			if(isInDebugMode)
				System.out.println("[?] array: new: " + this.arrayNameLiteral);
			
			return;
		}
		// Array end check!
		if(!this.isInStringLiteral && this.isInArray && (cChar == ']')){
			
			if(this.lastCompletedStringLiteral != null){
				this.arrayLiteralBuffer.add(this.lastCompletedStringLiteral);
				this.lastCompletedStringLiteral = null;
			} else
				System.err.println("Warning: Array without any elements created! @" + this.currentLineNumber + ":" + this.currentRowNumber);
			
			// Create Array
			
			if(isInDebugMode)
				System.out.println("[?] array: end: " + this.arrayNameLiteral);
			
			this.blockStack.peek().addArrayElement(this.arrayNameLiteral, this.arrayLiteralBuffer);
			
			// null data
			this.arrayNameLiteral = null;
			this.lastCompletedStringLiteral = null;
			this.arrayLiteralBuffer = null;
			this.isInArray = false;
			
			return;
		}
		// Array comma separator check!
		if(!this.isInStringLiteral && this.isInArray && (cChar == ',')){
			
			if(this.lastCompletedStringLiteral != null){
				this.arrayLiteralBuffer.add(this.lastCompletedStringLiteral);
				this.lastCompletedStringLiteral = null;
			} else
				throw new IllegalArgumentException("Can't push array element: No string literal set! @" + this.currentLineNumber + ":" + this.currentRowNumber);
			return;
		}
		
		// Key/Value element semicolon (end) check!
		if(!this.isInStringLiteral && (cChar == ';')){
			String value = this.stringLiteralQueue.peek();
			String key = this.stringLiteralQueue.peekSecond();
			
			if(value == null)
				throw new IllegalArgumentException("Can't create key/value element: No string literal for value set! @" + this.currentLineNumber + ":" + this.currentRowNumber);
			if(key == null)
				throw new IllegalArgumentException("Can't create key/value element: No string literal for key set! @" + this.currentLineNumber + ":" + this.currentRowNumber);
			
			this.blockStack.peek().addKeyValueElement(key,value);
			
			this.lastCompletedStringLiteral = null;
			return;
		}
		
		// If in String, add char to string-literal-buffer
		if(this.isInStringLiteral){
			this.stringLiteralBuffer.append(cChar);
			return;
		}
		
		// This point in the method is only reached if we are not inside a string-literal,
		// and the character-to-check is not a control-character.
	}
	
	/**
	 * A PTDS-Block, which stores elements and block-child's. It also has a name.
	 * 
	 * IDEA: Let PTDSBlock implement IPTDSElement!
	 **/
	public static class PTDSBlock {
		/** The parent of this PTDS-Block. **/
		private PTDSBlock blockParent;
		
		/** The name/key of this PTDS-Block. **/
		private String blockLabel;
		
		/** The HashMap that contains the data-element's of this PTDS-Block. **/
		private HashMap<String,IPTDSElement> blockElements;
		
		/** The HashMap that contains the child's of this PTDS-Block. **/
		private HashMap<String,PTDSBlock> blockChilds;
		
		/**  Create's a new PTDS-Block with the given name (key). **/
		public PTDSBlock(String blockLabel) {
			// save data
			this.blockParent = null;
			this.blockLabel = blockLabel;
			
			this.blockElements = new HashMap<String,IPTDSElement>();
			this.blockChilds = new HashMap<String,PTDSBlock>();
			
			if(isInDebugMode)
				System.out.println("["+this.getName()+"] created!");
		}
		
		/**  Create's a new PTDS-Block with the given name (key) and parent. **/
		public PTDSBlock(PTDSBlock blockParent, String blockLabel) {
			// save data
			this.blockParent = blockParent;
			this.blockLabel = blockLabel;
			
			this.blockElements = new HashMap<String,IPTDSElement>();
			this.blockChilds = new HashMap<String,PTDSBlock>();
			
			// add to parent
			this.blockParent.addChildBlock(this);
			
			if(isInDebugMode)
				System.out.println("["+this.getName()+"] created!");
		}
		
		/**
		 * @return the name of this PTDS-Block.
		 **/
		public String getName() {
			return this.blockLabel;
		}
		
		/**
		 * @return the parent PTDS-Block of this PTDS-Block. Can return null if at the head of the document-tree!
		 **/
		public PTDSBlock getParent(){
			return this.blockParent;
		}
		
		/** Set's the parent of this PTDS-Block.
		 * @param newBlockParent The new parent for this PTDS-Block. **/
		public void setParent(PTDSBlock newBlockParent){
			this.blockParent.blockChilds.remove(this.blockLabel);
			this.blockParent = newBlockParent;
			this.blockParent.addChildBlock(this);
		}
		
		/** Debug-method for printing the entire document to the console in a simplified tree form. **/
		public void printToConsole(String prefix) {
			System.out.println(prefix + " block: " + this.getName());
			
			for(IPTDSElement element : this.blockElements.values())
				System.out.println(prefix + "  element: " + element.toString());
			
			for(PTDSBlock child : this.blockChilds.values())
				child.printToConsole(prefix + ' ');
		}
		
		
		
		
		
		
		
		
		/** Add's a new Array-Element. **/
		public void addArrayElement(String name, ArrayList<String> arrayLiteralBuffer) {
			PTDSArrayElement array = new PTDSArrayElement(name, arrayLiteralBuffer);
			this.blockElements.put(name, array);

			if(isInDebugMode)
				System.out.println("["+this.getName()+"] added element: array '"+name+"'");
		}
		
		/** Add's a new Array-Element. **/
		public void addArrayElement(String name, String... arrayLiteralBuffer) {
			PTDSArrayElement array = new PTDSArrayElement(name, arrayLiteralBuffer);
			this.blockElements.put(name, array);

			if(isInDebugMode)
				System.out.println("["+this.getName()+"] added element: array '"+name+"'");
		}
		
		/** Add's a new PTDS-Block to this block as a child. **/
		public void addChildBlock(PTDSBlock block) {
			this.blockChilds.put(block.getName(), block);

			if(isInDebugMode)
				System.out.println("["+this.getName()+"] added element: block '"+block.getName()+"'");
		}
		
		/** Add's a new Key/Value element to this PTDS-Block. **/
		public void addKeyValueElement(String key, String value) {
			this.blockElements.put(key, new PTDSKeyValueElement(key,value));

			if(isInDebugMode)
				System.out.println("["+this.getName()+"] added element: keyValue '"+key+"'");
		}
		
		
		
		
		
		
		/** @return the HashMap that is used to store the block-child's of this PTDS-Block. **/
		public HashMap<String,PTDSBlock> getChildBlockHashMap(){
			return this.blockChilds;
		}
		
		/** @return the HashMap that is used to store the data-elements of this PTDS-Block. **/
		public HashMap<String,IPTDSElement> getElementHashMap(){
			return this.blockElements;
		}
		
		
		
		
		
		
		public boolean containsElementKey(String arg0){
			return this.blockElements.containsKey(arg0);
		}
		
		public boolean containsChildBlock(String arg0){
			return this.blockChilds.containsKey(arg0);
		}
		
		public boolean containsArrayElement(String arg0){
			IPTDSElement entry = this.blockElements.get(arg0);
			
			if(entry instanceof PTDSArrayElement)
				return true;
			else
				return false;
		}
		
		public boolean containsKeyValueElement(String arg0){
			IPTDSElement entry = this.blockElements.get(arg0);
			
			if(entry instanceof PTDSKeyValueElement)
				return true;
			else
				return false;
		}
		
		
		
		
		
		
		public PTDSBlock getChild(String arg0)
		{
			return this.blockChilds.get(arg0);
		}
		
		public String getKeyValue(String arg0)
		{
			IPTDSElement entry = this.blockElements.get(arg0);
			
			if(entry == null)
				throw new RuntimeException("The element '"+arg0+"' does not exist!");
			
			if(entry instanceof PTDSKeyValueElement)
				return ((PTDSKeyValueElement) entry).getValue();
			else
				throw new RuntimeException("The element '"+arg0+"' does exist, but is NOT a KeyValue!");
		}
		
		public String[] getArrayContents(String arg0)
		{
			IPTDSElement entry = this.blockElements.get(arg0);
			
			if(entry == null)
				throw new RuntimeException("The element '"+arg0+"' does not exist!");
			
			if(entry instanceof PTDSArrayElement)
				return ((PTDSArrayElement) entry).getArray();
			else
				throw new RuntimeException("The element '"+arg0+"' does exist, but is NOT a KeyValue!");
		}
		
		public PTDSArrayElement getArray(String arg0)
		{
			IPTDSElement entry = this.blockElements.get(arg0);
			
			if(entry == null)
				throw new RuntimeException("The element '"+arg0+"' does not exist!");
			
			if(entry instanceof PTDSArrayElement)
				return ((PTDSArrayElement) entry);
			else
				throw new RuntimeException("The element '"+arg0+"' does exist, but is NOT a KeyValue!");
		}
		
		public float getKeyValueAsFloat(String arg0)
		{
			return Float.valueOf(this.getKeyValue(arg0));
		}
		
		public double getKeyValueAsDouble(String arg0)
		{
			return Float.valueOf(this.getKeyValue(arg0));
		}
		
		public int getKeyValueAsInt(String arg0)
		{
			return Integer.valueOf(this.getKeyValue(arg0));
		}
		
		public boolean getKeyValueAsBoolean(String arg0)
		{
			return Boolean.valueOf(this.getKeyValue(arg0));
		}
		
		public char getKeyValueAsChar(String arg0)
		{
			return this.getKeyValue(arg0).charAt(0);
		}
		
		public int[] getArrayAsIntArray(String arg0)
		{
			return this.getArray(arg0).getArrayAsIntArray();
		}
		
		public float[] getArrayAsFloatArray(String arg0)
		{
			return this.getArray(arg0).getArrayAsFloatArray();
		}
		
		public double[] getArrayAsDoubleArray(String arg0)
		{
			return this.getArray(arg0).getArrayAsDoubleArray();
		}
		
		public boolean[] getArrayAsBooleanArray(String arg0)
		{
			return this.getArray(arg0).getArrayAsBooleanArray();
		}
		
	}
	
	/**A (named) element which can be stored inside a PTDS-Block.**/
	public static interface IPTDSElement {
		
		/**Get's the name (key) of this element.**/
		public String getName();
	}
	
	/** A PTDS-Element which stores multiple strings as a array.**/
	public static class PTDSArrayElement implements IPTDSElement {
		private String key;
		private String[] values;
		
		/**
		 * Creates a new array element with the given name (/key) and values.<br>
		 **/
		public PTDSArrayElement(String key, Collection<String> values) {
			this.key = key;
			this.values = new String[values.size()];
			values.toArray(this.values);
		}
		
		/**
		 * Creates a new array element with the given name (/key) and values.<br>
		 * Be aware that this method makes a copy of the given array.<br>
		 **/
		public PTDSArrayElement(String key, String[] values) {
			this.key = key;
			this.values = new String[values.length];
			System.arraycopy(values, 0, this.values, 0, values.length);
		}
		
		@Override
		public String getName() {
			return this.key;
		}
		
		/** @return the array this element uses. **/
		public String[] getArray(){
			return this.values;
		}
		
		/**@return the array-item at the given position inside this array-element. **/
		public String getArrayElement(int index){
			return this.values[index];
		}
		
		/**@return the array-item at the given position inside this array-element. **/
		public int getArrayElementAsInt(int index){
			return Integer.valueOf(this.values[index]);
		}
		
		/**@return the array-item at the given position inside this array-element. **/
		public float getArrayElementAsFloat(int index){
			return Float.valueOf(this.values[index]);
		}
		
		/**@return the array-item at the given position inside this array-element. **/
		public double getArrayElementAsDouble(int index){
			return Double.valueOf(this.values[index]);
		}
		
		/**@return the array-item at the given position inside this array-element. **/
		public boolean getArrayElementAsBoolean(int index){
			return Boolean.valueOf(this.values[index]);
		}
		
		public int[] getArrayAsIntArray()
		{
			int[] n = new int[this.values.length];
			
			for(int i = 0; i < n.length; i++)
				n[i] = Integer.valueOf(this.values[i]);
			
			return n;
		}
		
		public float[] getArrayAsFloatArray()
		{
			float[] n = new float[this.values.length];
			
			for(int i = 0; i < n.length; i++)
				n[i] = Float.valueOf(this.values[i]);
			
			return n;
		}
		
		public double[] getArrayAsDoubleArray()
		{
			double[] n = new double[this.values.length];
			
			for(int i = 0; i < n.length; i++)
				n[i] = Double.valueOf(this.values[i]);
			
			return n;
		}
		
		public boolean[] getArrayAsBooleanArray()
		{
			boolean[] n = new boolean[this.values.length];
			
			for(int i = 0; i < n.length; i++)
				n[i] = Boolean.valueOf(this.values[i]);
			
			return n;
		}
		
		@Override
		public String toString(){
			return "array{"+this.key+","+Arrays.toString(this.values)+"}";
		}
	}
	
	/**
	 * A Key/Value element that has a name (the key) and a single value.
	 **/
	public static class PTDSKeyValueElement implements IPTDSElement {
		private String key;
		private String value;
		
		public PTDSKeyValueElement(String key, String value) {
			this.key = key;
			this.value = value;
		}
		
		@Override
		public String getName(){
			return this.key;
		}
		
		/** @return The value this Key/Value-element contains. **/
		public String getValue(){
			return this.value;
		}
		
		/** Set's the value of this Key/Value-element. **/
		public void setValue(String newValue){
			this.value = newValue;
		}
		
		@Override
		public String toString(){
			return "keyValue{"+this.key+","+this.value+"}";
		}
	}
	
}

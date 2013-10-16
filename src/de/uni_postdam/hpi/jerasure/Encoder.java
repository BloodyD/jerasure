package de.uni_postdam.hpi.jerasure;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import de.uni_postdam.hpi.cauchy.Cauchy;
import de.uni_postdam.hpi.matrix.*;
import de.uni_postdam.hpi.utils.CalcUtils;
import de.uni_postdam.hpi.utils.CodingUtils;
import de.uni_postdam.hpi.utils.FileUtils;

public class Encoder {

	int k, m, w;

	Matrix matrix = null;
	BitMatrix bitMatrix = null;
	Schedule[] schedules = null;

	int blockSize, bufferSize, packetSize, codingBlockSize;

	public Encoder(int k, int m, int w) {
		this.k = k;
		this.m = m;
		this.w = w;

		this.matrix = Cauchy.good_general_coding_matrix(k, m, w);
		this.bitMatrix = new BitMatrix(matrix, w);
		this.schedules = bitMatrix.toSchedules(k, w);
	}

	public byte[] encode(byte[] data, int packetSize) {
		return CodingUtils.enOrDecode(data, schedules, k, m, w, packetSize);
	}

	public byte[] encode(Buffer data, int packetSize) {
		return CodingUtils.enOrDecode(data, schedules, k, m, w, packetSize);
	}
	
	
	public void encode(Buffer data, Buffer coding, int packetSize) {
		data.setLen(k * packetSize * w);
		coding.setLen(m * packetSize * w);
		Schedule.do_scheduled_operations(data, coding, schedules, packetSize, w);
	}
	
	private void encode(Buffer data, Buffer coding){
		this.encode(data, coding, packetSize);
	}

	private void calcSizes(long size) {
		packetSize = CalcUtils.calcPacketSize(k, w, size);
		bufferSize = CalcUtils.calcBufferSize(k, w, packetSize, size);
		blockSize = CalcUtils.calcBlockSize(k, w, packetSize);
		codingBlockSize = CalcUtils.calcCodingBlockSize(m, w, packetSize);
	}


	
	public void encode(File file) {
		if (!file.exists()) {
			throw new IllegalArgumentException("File " + file.getAbsolutePath()
					+ " does not exist!");
		}
		FileInputStream fis = null;
		FileOutputStream[] k_parts = null;
		FileOutputStream[] m_parts = null;
		
		try {
			fis = new FileInputStream(file);
			calcSizes(file.length());
//			System.out.print("buffer:" + bufferSize + "\t");
//			System.out.print("block:" + blockSize + "\t");
//			System.out.println("packet:" + packetSize + "\t");
			k_parts = FileUtils.createParts(file.getAbsolutePath(), "k", k);
			m_parts = FileUtils.createParts(file.getAbsolutePath(), "m", m);
			Buffer data = new Buffer(bufferSize);
			Buffer coding = new Buffer(bufferSize / k * m);
			int numRead = 0;

			while ((numRead = data.readFromStream(fis)) >= 0) {
				data.reset();
				coding.reset();
				
				performEncoding(data, coding);
				// encode last blocks
				if (bufferSize != numRead) {
					performLastReadEncoding(data, coding, numRead);
				}

				data.setStart(0);
				coding.setStart(0);
				
				FileUtils.writeParts(data, coding, k_parts, m_parts, w, packetSize);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			FileUtils.close(k_parts);
			FileUtils.close(m_parts);
		}

	}

	private void performLastReadEncoding(Buffer data, Buffer coding, int numRead) {

		data.setRange(numRead / blockSize * blockSize, numRead % blockSize);
		coding.setRange(numRead / blockSize * codingBlockSize, numRead % blockSize);
		
		encode(data, coding);		
	}

	private void performEncoding(Buffer data, Buffer coding) {
		int steps = bufferSize / blockSize;
		for (int i = 0; i < steps; i++) {
			data.setRange(i * blockSize, blockSize);
			coding.setRange(i * codingBlockSize, codingBlockSize);
			
			encode(data, coding);
		}	
	}
	
/*	public void encode2(File file) {
	if (!file.exists()) {
		throw new IllegalArgumentException("File " + file.getAbsolutePath()
				+ " does not exist!");
	}
	FileInputStream fis = null;
	FileOutputStream[] k_parts = null;
	FileOutputStream[] m_parts = null;

	try {
		fis = new FileInputStream(file);
		calcSizes(file.length());
		//	System.out.print("buffer:" + bufferSize + "\t");
		//	System.out.print("block:" + blockSize + "\t");
		//	System.out.println("packet:" + packetSize + "\t");
		k_parts = FileUtils.createParts(file.getAbsolutePath(), "k", k);
		m_parts = FileUtils.createParts(file.getAbsolutePath(), "m", m);
		Buffer newBuffer = new Buffer(bufferSize);
		int numRead = 0;

		while ((numRead = newBuffer.readFromStream(fis)) >= 0) {
			newBuffer.reset();
			if (bufferSize != numRead) {
				newBuffer.setEnd(numRead);
				performLastReadEncoding(newBuffer, k_parts, m_parts);
			} else {
				performEncoding(newBuffer, k_parts, m_parts);
			}
		}
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	} finally {
		try {
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		FileUtils.close(k_parts);
		FileUtils.close(m_parts);
	}

}
	private void performEncoding(byte[] buffer, FileOutputStream[] k_parts,
			FileOutputStream[] m_parts, int w2, Schedule[] schedules2) throws IOException {
		for (int i = 0; i < buffer.length / blockSize; i++) {
			encodeAndWrite(Arrays.copyOfRange(buffer, i * blockSize, (i + 1) * blockSize), k_parts, m_parts);
		}		
	}

	private void performLastReadEncoding(byte[] buffer,
			FileOutputStream[] k_parts, FileOutputStream[] m_parts, int w2,
			Schedule[] schedules2) throws IOException {
			performEncoding(buffer, k_parts, m_parts, w, schedules);
			int start = (buffer.length / blockSize) * blockSize;
			int end = start + buffer.length % blockSize;
			if (start == 0) {
				encodeAndWrite(buffer, k_parts, m_parts);
			} else {
				encodeAndWrite(Arrays.copyOfRange(buffer, start, end), k_parts, m_parts);
			}		
	}
	
	private void encodeAndWrite(byte[] data, FileOutputStream[] k_parts,
			FileOutputStream[] m_parts) throws IOException {

		int blockSize = CalcUtils.calcBlockSize(k, w, packetSize);
		data = CodingUtils.addPaddingIfNeeded(data, blockSize);
		byte[] coding = encode(data, packetSize);
		FileUtils.writeParts(data, coding, k_parts, m_parts, w, packetSize);

	}

	private void performEncoding(Buffer buffer, FileOutputStream[] k_parts,
			FileOutputStream[] m_parts) throws IOException {
		int bufferSize = buffer.size();

		for (int i = 0; i < bufferSize / blockSize; i++) {
			buffer.setStart(i * blockSize);
			buffer.setEnd((i + 1) * blockSize);
			encodeAndWrite(buffer, k_parts, m_parts);
		}

	}

	private void performLastReadEncoding(Buffer buffer,
			FileOutputStream[] k_parts, FileOutputStream[] m_parts) throws IOException {

		int bufferSize = buffer.size();
		performEncoding(buffer, k_parts, m_parts);
		int start = (bufferSize / blockSize) * blockSize;
		int end = start + bufferSize % blockSize;
		buffer.setStart(start);
		buffer.setEnd(end);
		encodeAndWrite(buffer, k_parts, m_parts);
	}

	private void encodeAndWrite(Buffer data, FileOutputStream[] k_parts,
			FileOutputStream[] m_parts) throws IOException {
		byte[] coding = encode(data, packetSize);
		FileUtils.writeParts(data, coding, k_parts, m_parts, w, packetSize);
	}
	*/
}

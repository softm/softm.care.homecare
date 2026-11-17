package net.softm.data.kata.god.dto;

import static net.softm.data.kata.util.Utils.encdodeForWeb;

import com.google.firebase.Timestamp;

/**
 * PrayMemo
 * @author softm 
 */
public class PrayMemo extends PrayBase {
	private static final String TAG = "PrayMemo";
	public PrayMemo() {
		super();
	}
	public PrayMemo(Timestamp timestampId) {
		super();
		this.timestampId = timestampId;
	}
	private void encodePayMemo() {
		setMemo(encdodeForWeb(memo));
	}
	public Timestamp timestampId; //

	public String classDocId; //

	private String memo; //

	public Timestamp getTimestampId() {
		return timestampId;
	}

	public void setTimestampId(Timestamp timestampId) {
		this.timestampId = timestampId;
	}

	public String getClassDocId() {
		return classDocId;
	}

	public void setClassDocId(String classDocId) {
		this.classDocId = classDocId;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	@Override
	public void gen() {
		super.gen();
		encodePayMemo();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		PrayMemo other = (PrayMemo) obj;
		return this.docId != null && this.docId.equals(other.docId); // docId가 같은지 비교
	}

	@Override
	public int hashCode() {
		return docId != null ? docId.hashCode() : 0; // docId를 기준으로 hashCode를 계산
	}
}

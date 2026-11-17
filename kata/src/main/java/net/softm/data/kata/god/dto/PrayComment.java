package net.softm.data.kata.god.dto;

import static net.softm.data.kata.util.Utils.encdodeForWeb;

/**
 * PrayComment
 * @author softm 
 */
public class PrayComment extends PrayBase {
	private static final String TAG = "PrayComment";
	public PrayComment() {
		super();
	}
	public PrayComment(String classDocId) {
		super();
		this.classDocId = classDocId;
	}

	public String classDocId; //

	private String comment; //

	public String getClassDocId() {
		return classDocId;
	}

	public void setClassDocId(String classDocId) {
		this.classDocId = classDocId;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public void gen() {
		super.gen();
		encodePayComment();
	}

	private void encodePayComment() {
		setComment(encdodeForWeb(comment));
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		PrayComment other = (PrayComment) obj;
		return this.docId != null && this.docId.equals(other.docId); // docId가 같은지 비교
	}

	@Override
	public int hashCode() {
		return docId != null ? docId.hashCode() : 0; // docId를 기준으로 hashCode를 계산
	}
}

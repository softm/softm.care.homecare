package net.softm.data.kata.dto;

import com.google.gson.annotations.SerializedName;

/**
 * ServerStateDTOOut
 * 기본DTO
 * @author softm
 * {"_msg_":{"_common_":{"error.code":"F0000","resCode":"00"},"_body_":{"status":"success","flag":"S"}}}
 */
public class ServerStateDTOOut extends BaseDTO {
	public mainDTO _msg_ = new mainDTO();
	public ServerStateDTOOut() {
		super();
	}

	public String state; //

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public mainDTO get_msg_() {
		return _msg_;
	}

	public void set_msg_(mainDTO _msg_) {
		this._msg_ = _msg_;
	}

	public class mainDTO {
		public commonDTO _common_= new commonDTO();
		public bodyDTO _body_ = new bodyDTO();

		public commonDTO get_common_() {
			return _common_;
		}

		public void set_common_(commonDTO _common_) {
			this._common_ = _common_;
		}

		public bodyDTO get_body_() {
			return _body_;
		}

		public void set_body_(bodyDTO _body_) {
			this._body_ = _body_;
		}
	}

	public class commonDTO {
		@SerializedName("error.code")
		public String error_code=null;
		public String resCode=null;

		public String getResCode() {
			return resCode;
		}

		public void setResCode(String resCode) {
			this.resCode = resCode;
		}

		public String getError_code() {
			return error_code;
		}

		public void setError_code(String error_code) {
			this.error_code = error_code;
		}
	}
	public class bodyDTO {
		public String status=null;
		public String flag=null;

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public String getFlag() {
			return flag;
		}

		public void setFlag(String flag) {
			this.flag = flag;
		}
	}

}


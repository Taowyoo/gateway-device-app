/**
 * This class is part of the Programming the Internet of Things project.
 * <p>
 * It is provided as a simple shell to guide the student and assist with
 * implementation for the Programming the Internet of Things exercises,
 * and designed to be modified by the student as needed.
 */

package programmingtheiot.data;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import programmingtheiot.common.ConfigConst;

/**
 * Shell representation of class for student implementation.
 *
 */
public abstract class BaseIotData implements Serializable {
    // static

    public static final float DEFAULT_VAL = 0.0f;
    public static final int DEFAULT_STATUS = 0;

    // private var's

    private String name = ConfigConst.NOT_SET;
    private String timeStamp = null;
    private boolean hasError = false;
    private int statusCode = DEFAULT_STATUS;


    // constructors

    /**
     * Default Constructor
     */
    protected BaseIotData() {
        super();
        this.updateTimeStamp();
    }


    // public methods

    public String getName() {
        return name;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public long getTimeStampMillis() {
        long timeStampMillis = 0L;
        try {
            timeStampMillis =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS").parse(this.timeStamp).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timeStampMillis;
    }

    public boolean hasErrorFlag() {
        return hasError;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStatusCode(int code) {
        this.statusCode = code;
    }

    public void updateTimeStamp(){
        Date date = new Date();
        this.timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS").format(date);
    }

	/**
	 * Use given BaseIotData instance to update current instance
	 * @param data Given BaseIotData instance (including subclass instance)
	 */
	public void updateData(BaseIotData data) {
        // update local super class variables
        this.name = data.name;
        this.timeStamp = data.timeStamp;
        this.hasError = data.hasError;
        this.statusCode = data.statusCode;
        // update subclass variables
        handleUpdateData(data);
    }

    @Override
    public String toString() {
        return "BaseIotData{" +
                "name='" + name + '\'' +
                ", timeStamp='" + timeStamp + '\'' +
                ", hasError=" + hasError +
                ", statusCode=" + statusCode +
                '}';
    }

    // protected methods

    /**
     * Template method to handle data update for the sub-class.
     *
     * @param data While the parameter must implement this method,
     * the sub-class is expected to cast the base class to its given type.
     */
    protected abstract void handleUpdateData(BaseIotData data);

}

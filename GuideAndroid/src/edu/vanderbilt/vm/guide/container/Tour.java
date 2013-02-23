
package edu.vanderbilt.vm.guide.container;

import java.util.List;

import edu.vanderbilt.vm.guide.annotations.NeedsTesting;

@NeedsTesting(lastModifiedDate = "12/22/12")
public class Tour {

    /**
     * The agenda that contains the places on the tour
     */
    private Agenda mAgenda;

    private String mTimeRequired;

    private String mDistance;

    private String mDescription;

    private String mName;

    private final int mUniqueId;

    private static final int DEFAULT_ID = -1;

    /**
     * A string containing a URI that points to the location of the icon for
     * this tour
     */
    private String mIconLoc;

    /* package */Tour() {
        mUniqueId = DEFAULT_ID;
    }

    public Tour(Agenda agenda) {
        this();
        mAgenda = agenda;
    }

    private Tour(Tour.Builder builder) {
        this.mAgenda = builder.mAgenda;
        this.mTimeRequired = builder.mTimeRequired;
        this.mDescription = builder.mDescription;
        this.mDistance = builder.mDistance;
        this.mName = builder.mName;
        this.mIconLoc = builder.mIconLoc;
        this.mUniqueId = builder.mUniqueId;
    }

    public Tour(List<Place> placesOnTour) {
        this();
        mAgenda = new Agenda(placesOnTour);
    }

    public void setName(String name) {
        mName = name;
    }

    public void setTimeReq(String timeReq) {
        mTimeRequired = timeReq;
    }

    public void setDistance(String dist) {
        mDistance = dist;
    }

    public void setDescription(String desc) {
        mDescription = desc;
    }

    public String getName() {
        return mName;
    }

    public String getTimeReq() {
        return mTimeRequired;
    }

    public String getDistance() {
        return mDistance;
    }

    public String getDescription() {
        return mDescription;
    }

    public Agenda getAgenda() {
        return mAgenda;
    }

    public String getIconLoc() {
        return mIconLoc;
    }

    public int getUniqueId() {
        return mUniqueId;
    }

    public static class Builder {
        private Agenda mAgenda;

        private String mTimeRequired;

        private String mDistance;

        private String mDescription;

        private String mName;

        private String mIconLoc;

        private int mUniqueId = DEFAULT_ID;

        public Builder() {
        }

        public Builder setAgenda(Agenda agenda) {
            mAgenda = agenda;
            return this;
        }

        public Builder setTimeReq(String timeReq) {
            mTimeRequired = timeReq;
            return this;
        }

        public Builder setDistance(String distance) {
            mDistance = distance;
            return this;
        }

        public Builder setDescription(String desc) {
            mDescription = desc;
            return this;
        }

        public Builder setName(String name) {
            mName = name;
            return this;
        }

        public Builder setIconLoc(String iconLoc) {
            mIconLoc = iconLoc;
            return this;
        }

        public Builder setUniqueId(int uniqueId) {
            mUniqueId = uniqueId;
            return this;
        }

        public Tour build() {
            return new Tour(this);
        }

    }

}

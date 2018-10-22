package org.superbiz.moviefun.albums;

import java.sql.Timestamp;

public class AlbumSchedulerTask {

    private Timestamp started_at;

    public AlbumSchedulerTask(Timestamp started_at) {
        this.started_at = started_at;
    }

    public Timestamp getStarted_at() {
        return started_at;
    }

    public void setStarted_at(Timestamp started_at) {
        this.started_at = started_at;
    }
}

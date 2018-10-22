package org.superbiz.moviefun.albums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.sql.DataSource;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Calendar;

@Configuration
@EnableAsync
@EnableScheduling
public class AlbumsUpdateScheduler {

    private static final long SECONDS = 1000;
    private static final long MINUTES = 60 * SECONDS;

    private final AlbumsUpdater albumsUpdater;
    private JdbcTemplate jdbcTemplate;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public AlbumsUpdateScheduler(AlbumsUpdater albumsUpdater, JdbcTemplate jdbcTemplate) {
        this.albumsUpdater = albumsUpdater;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Scheduled(initialDelay = 15 * SECONDS, fixedRate = 2 * MINUTES)
    public void run() {
        try {
            if (startAlbumSchedulerTask()) {
                logger.debug("Starting albums update");

                albumsUpdater.update();

                logger.debug("Finished albums update");

            } else {
                logger.debug("Nothing to start");
            }

        } catch (Throwable e) {
            logger.error("Error while updating albums", e);
        }
    }

    private boolean startAlbumSchedulerTask() {

        String sqlString = "update album_scheduler_task set started_at = now() WHERE started_at IS NULL OR started_at < date_sub(now(), INTERVAL 2 MINUTE)";
        return (jdbcTemplate.update(sqlString) > 0);
    }

    private final RowMapper<AlbumSchedulerTask> mapper = (rs, rowNum) -> new AlbumSchedulerTask(
            rs.getTimestamp("started_at")
    );

    private final ResultSetExtractor<AlbumSchedulerTask> extractor =
            (rs) -> rs.next() ? mapper.mapRow(rs, 1) : null;
}

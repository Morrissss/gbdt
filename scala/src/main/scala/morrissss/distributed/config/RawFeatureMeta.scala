package morrissss.distributed.config

import java.time.LocalDateTime

import morrissss.base.util.DateUtils
import org.apache.spark.sql.Row

import scala.collection.mutable


abstract class FeatureField(val name: String)
case object INFO extends FeatureField("info")
case object ENTRY extends FeatureField("entry")

abstract class FeatureType(val name: String)
case object DOUBLE extends FeatureType("DOUBLE")
case object INT extends FeatureType("INT")
case object STRING extends FeatureType("STRING")

class FeatureMeta(val featureField: FeatureField,
                  val featureType: FeatureType,
                  val defaultValue: String) {
}

class RawFeatureMeta {

}

object RawFeatureMeta {
    val FEATURE_METAS = mutable.LinkedHashMap[String, FeatureMeta]("newUser" -> new FeatureMeta(INFO, DOUBLE, "0"),

                                                                   "aLike" -> new FeatureMeta(ENTRY, DOUBLE, "0"),
                                                                   "aComment" -> new FeatureMeta(ENTRY, DOUBLE, "0"),
                                                                   "aCollect" -> new FeatureMeta(ENTRY, DOUBLE, "0"),

                                                                   "nWord" -> new FeatureMeta(ENTRY, DOUBLE, "0"),
                                                                   "nPic" -> new FeatureMeta(ENTRY, DOUBLE, "0"),
                                                                   "nPicLabel" -> new FeatureMeta(ENTRY, DOUBLE, "0"),

                                                                   "nImp" -> new FeatureMeta(ENTRY, DOUBLE, "0"),
                                                                   "nClick" -> new FeatureMeta(ENTRY, DOUBLE, "0"),
                                                                   "nLike" -> new FeatureMeta(ENTRY, DOUBLE, "0"),
                                                                   "nCollect" -> new FeatureMeta(ENTRY, DOUBLE, "0"),

                                                                   "nGdImp" -> new FeatureMeta(ENTRY, DOUBLE, "0"),
                                                                   "nGdClk" -> new FeatureMeta(ENTRY, DOUBLE, "0"),
                                                                   "nGdLike" -> new FeatureMeta(ENTRY, DOUBLE, "0"),
                                                                   "nGdCol" -> new FeatureMeta(ENTRY, DOUBLE, "0"),
                                                                   "nCtImp" -> new FeatureMeta(ENTRY, DOUBLE, "0"),
                                                                   "nCtClk" -> new FeatureMeta(ENTRY, DOUBLE, "0"),
                                                                   "nCtLike" -> new FeatureMeta(ENTRY, DOUBLE, "0"),
                                                                   "nCtCol" -> new FeatureMeta(ENTRY, DOUBLE, "0"),
                                                                   "nPfImp" -> new FeatureMeta(ENTRY, DOUBLE, "0"),
                                                                   "nPfClk" -> new FeatureMeta(ENTRY, DOUBLE, "0"),
                                                                   "nPfLike" -> new FeatureMeta(ENTRY, DOUBLE, "0"),
                                                                   "nPfCol" -> new FeatureMeta(ENTRY, DOUBLE, "0"),
                                                                   "nMfImp" -> new FeatureMeta(ENTRY, DOUBLE, "0"),
                                                                   "nMfClk" -> new FeatureMeta(ENTRY, DOUBLE, "0"),
                                                                   "nMfLike" -> new FeatureMeta(ENTRY, DOUBLE, "0"),
                                                                   "nMfCol" -> new FeatureMeta(ENTRY, DOUBLE, "0"),
                                                                   "nGpImp" -> new FeatureMeta(ENTRY, DOUBLE, "0"),
                                                                   "nGpClk" -> new FeatureMeta(ENTRY, DOUBLE, "0"),
                                                                   "nGpLike" -> new FeatureMeta(ENTRY, DOUBLE, "0"),
                                                                   "nGpCol" -> new FeatureMeta(ENTRY, DOUBLE, "0"),

                                                                   "vaGender" -> new FeatureMeta(ENTRY, INT, "0"),
                                                                   "vaCity" -> new FeatureMeta(ENTRY, DOUBLE, "0"),
                                                                   "vaAge" -> new FeatureMeta(ENTRY, DOUBLE, "0"),
                                                                   "vaGroup" -> new FeatureMeta(ENTRY, DOUBLE, "0"),
                                                                   "vaTaste" -> new FeatureMeta(ENTRY, DOUBLE, "0"),
                                                                   "vaFollow" -> new FeatureMeta(ENTRY, DOUBLE, "0"),
                                                                   "vaFTag" -> new FeatureMeta(ENTRY, DOUBLE, "0"),

                                                                   "vnLTLike" -> new FeatureMeta(ENTRY, DOUBLE, "0"),
                                                                   "vnLTCom" -> new FeatureMeta(ENTRY, DOUBLE, "0"),
                                                                   "vnLTCol" -> new FeatureMeta(ENTRY, DOUBLE, "0"),
                                                                   "vnLTpLike" -> new FeatureMeta(ENTRY, DOUBLE, "0"),
                                                                   "vnLTpCom" -> new FeatureMeta(ENTRY, DOUBLE, "0"),
                                                                   "vnLTpCol" -> new FeatureMeta(ENTRY, DOUBLE, "0"),

                                                                   "vnSTSq" -> new FeatureMeta(ENTRY, DOUBLE, "0"),
                                                                   "vnSTFls" -> new FeatureMeta(ENTRY, DOUBLE, "0"),
                                                                   "vnSTSc" -> new FeatureMeta(ENTRY, DOUBLE, "0"),
                                                                   "vnSTopic" -> new FeatureMeta(ENTRY, DOUBLE, "0"),

                                                                   "vnTaste" -> new FeatureMeta(ENTRY, DOUBLE, "0"),
                                                                   "vnSTheme" -> new FeatureMeta(ENTRY, DOUBLE, "0"),

                                                                   "matchReasonR" -> new FeatureMeta(ENTRY, DOUBLE, "0"),
                                                                   "matchReasonB" -> new FeatureMeta(ENTRY, DOUBLE, "0")
                                                                  )

    val sqlTemplate =
        """
          |SELECT front.req_id AS req_id, click, liked, collect, share,
          |$INFO_FIELDS,
          |$ENTRY_FIELDS
          |FROM (
          |    SELECT SPLIT(track_id, '@')[1] AS req_id, action_target AS post_id,
          |           IF(click_num>0, 1, 0) AS click,
          |           IF(liked, 1, 0) AS liked,
          |           IF(collected, 1, 0) AS collect,
          |           IF(shared, 1, 0) AS share
          |    FROM reddw.dw_log_events_algo_hourly
          |    WHERE dtm BETWEEN '$START_DATE' AND '$STOP_DATE'
          |      AND page_key = 'homefeed'
          |      AND (page_instance IS NULL OR page_instance = '' OR page_instance = 'homefeed_recommend')
          |      AND action_type = 'impression'
          |      AND target_type = 'post'
          |      AND SIZE(SPLIT(track_id, '@')) = 2
          |) front
          |JOIN (
          |    SELECT track_id AS req_id, info, entry['noteId'] AS post_id, entry
          |    FROM (
          |        SELECT track_id, info, entries
          |        FROM redods.ods_log_homefeed_model
          |        WHERE dtm BETWEEN '$START_DTM' AND '$STOP_DTM'
          |    ) a
          |    LATERAL VIEW EXPLODE(entries) l AS entry
          |    WHERE entry['noteId'] IS NOT NULL
          |) back
          |ON front.req_id = back.req_id AND front.post_id = back.post_id
          |WHERE LENGTH(front.req_id) > 0
          |  AND LENGTH(front.post_id) > 0
        """.stripMargin

    def sql(startTime: LocalDateTime, stopTime: LocalDateTime): String = {
        val infoFields = new StringBuilder
        val entryFields = new StringBuilder
        for ((name, meta) <- FEATURE_METAS) {
            val target = meta.featureField match {
                case INFO => infoFields
                case ENTRY => entryFields
            }
            target ++= "       CAST(COALESCE(%s['%s'], '%s') AS %s) AS %s,\n".format(meta.featureField.name,
                                                                                     name, meta.defaultValue,
                                                                                     meta.featureType.name, name)
        }
        infoFields.delete(infoFields.length()-2, infoFields.length())
        entryFields.delete(entryFields.length()-2, entryFields.length())
        sqlTemplate.replace("$INFO_FIELDS", infoFields.toString)
                   .replace("$ENTRY_FIELDS", entryFields.toString)
                   .replace("$START_DATE", DateUtils.dateStr(startTime))
                   .replace("$STOP_DATE", DateUtils.dateStr(stopTime))
                   .replace("$START_DTM", DateUtils.dtmStr(startTime))
                   .replace("$STOP_DTM", DateUtils.dtmStr(stopTime))
    }

}
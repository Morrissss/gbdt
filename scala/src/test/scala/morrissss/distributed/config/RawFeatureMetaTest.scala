package morrissss.distributed.config

import java.time.LocalDateTime

import org.junit.Test
import org.scalatest.junit.AssertionsForJUnit

class RawFeatureMetaTest extends AssertionsForJUnit {

    @Test def test(): Unit = {
        println(RawFeatureMeta.sql(LocalDateTime.now, LocalDateTime.now))
    }
}

package morrissss.distributed

import org.apache.spark.sql.Row

trait RowLabeler extends Serializable {

    def label(row: Row): Int
}

class TreeModelLabeler(val column: String) extends RowLabeler {
    override def label(row: Row): Int = row.getAs(column)
}
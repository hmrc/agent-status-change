/*
 * Copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.agentstatuschange.services

import com.google.inject.Singleton
import com.mongodb.client.model.IndexOptions
import org.joda.time.DateTime
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.IndexModel
import org.mongodb.scala.model.Indexes.ascending
import uk.gov.hmrc.agentstatuschange.models.AgentStatusChangeRecord
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AgentStatusChangeMongoService @Inject()(mongoComponent: MongoComponent)(
    implicit ec: ExecutionContext)
    extends PlayMongoRepository[AgentStatusChangeRecord](
      mongoComponent = mongoComponent,
      collectionName = "agent-status-change",
      domainFormat = AgentStatusChangeRecord.format,
      indexes = Seq(
        IndexModel(ascending("arn", "lastUpdated"),
                   new IndexOptions().name("Arn_LastUpdated")))
    ) {

  implicit val ord: Ordering[DateTime] =
    Ordering.by(time => time.getMillis)

  def findCurrentRecordByArn(
      arn: String): Future[Option[AgentStatusChangeRecord]] = {

    val selector = equal("arn", arn)
    val descending = -1
    val sort = equal("lastUpdated", descending)

    collection
      .find(selector)
      .sort(sort)
      .headOption()
  }

  def createRecord(agentStatusChangeRecord: AgentStatusChangeRecord)(
      implicit ec: ExecutionContext): Future[Unit] =
    collection.insertOne(agentStatusChangeRecord).head().map(_ => ())
}

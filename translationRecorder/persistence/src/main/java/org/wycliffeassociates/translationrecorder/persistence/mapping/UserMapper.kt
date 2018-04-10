package org.wycliffeassociates.translationrecorder.persistence.mapping

import org.wycliffeassociates.translationrecorder.data.model.User
import org.wycliffeassociates.translationrecorder.persistence.entity.UserEntity

/**
 * Created by sarabiaj on 4/5/2018.
 */

class UserMapper() :  Mapper<UserEntity, User> {
    override fun mapFromEntity(type: UserEntity): User {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun mapToEntity(type: User): UserEntity {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
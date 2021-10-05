package com.notesync.notes.business.domain.util

interface EntityMapper <Entity, DomainModel>{

    fun mapFromEntity(entity: Entity,key:String?=null): DomainModel

    fun mapToEntity(domainModel: DomainModel,key:String?=null): Entity
}
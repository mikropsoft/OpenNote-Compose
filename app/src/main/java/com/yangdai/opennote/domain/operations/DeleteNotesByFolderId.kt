package com.yangdai.opennote.domain.operations

import com.yangdai.opennote.domain.repository.NoteRepository

class DeleteNotesByFolderId(
    private val repository: NoteRepository
) {

    suspend operator fun invoke(folderId: Long?) {
        repository.deleteNotesByFolderId(folderId)
    }
}
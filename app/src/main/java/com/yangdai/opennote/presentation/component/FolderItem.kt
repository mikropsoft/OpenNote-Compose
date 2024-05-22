package com.yangdai.opennote.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DriveFileRenameOutline
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yangdai.opennote.R
import com.yangdai.opennote.data.local.entity.FolderEntity

@Composable
fun LazyGridItemScope.FolderItem(
    folder: FolderEntity,
    onModify: (FolderEntity) -> Unit,
    onDelete: () -> Unit
) {

    var showModifyDialog by remember {
        mutableStateOf(false)
    }

    var showWarningDialog by remember {
        mutableStateOf(false)
    }

    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .animateItem(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Row(Modifier.weight(1f)) {
            Icon(
                modifier = Modifier.padding(horizontal = 16.dp),
                imageVector = Icons.Outlined.FolderOpen,
                tint = if (folder.color != null) Color(folder.color) else MaterialTheme.colorScheme.onSurface,
                contentDescription = "Leading Icon"
            )

            Text(
                text = folder.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row {
            IconButton(onClick = {
                showModifyDialog = !showModifyDialog
            }) {
                Icon(
                    imageVector = Icons.Outlined.DriveFileRenameOutline,
                    contentDescription = "Modify"
                )
            }

            IconButton(onClick = {
                showWarningDialog = !showWarningDialog
            }) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }

        if (showWarningDialog) {
            WarningDialog(
                message = stringResource(R.string.deleting_a_folder_will_also_delete_all_the_notes_it_contains_and_they_cannot_be_restored_do_you_want_to_continue),
                onDismissRequest = { showWarningDialog = false },
                onConfirm = onDelete
            )
        }

        if (showModifyDialog) {
            ModifyFolderDialog(
                folder = folder,
                onDismissRequest = { showModifyDialog = false }) {
                onModify(it)
            }
        }
    }
}

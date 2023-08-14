package dev.ridill.mym.settings.domain.backup

import android.content.Context
import com.google.android.gms.common.Scopes
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.FileList
import dev.ridill.mym.R
import dev.ridill.mym.core.domain.service.GoogleSignInService
import dev.ridill.mym.core.domain.util.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream

class GDriveService(
    private val context: Context,
    private val signInService: GoogleSignInService
) {
    @Throws(GoogleAccountNotSignedInThrowable::class, Throwable::class)
    suspend fun uploadBackupFile(file: File) = withContext(Dispatchers.IO) {
        val gFile = com.google.api.services.drive.model.File().apply {
            name = file.name
            parents = backupParents
        }
        val fileContent = FileContent(BACKUP_MIME_TYPE, file)

        val createdFile = getDriveService().Files().create(gFile, fileContent).execute()

        log { "UploadedFile - $createdFile" }
    }

    suspend fun deleteFile(fileId: String): Unit = withContext(Dispatchers.IO) {
        getDriveService().Files().delete(fileId).execute()
    }

    @Throws(GoogleAccountNotSignedInThrowable::class, Throwable::class)
    suspend fun getFilesList(): FileList = withContext(Dispatchers.IO) {
        getDriveService().Files().list().apply {
            orderBy = "recency"
            q = "name = '$BACKUP_FILE_NAME' and trashed=false"
            spaces = APP_FOLDER_SPACE
        }.execute()
    }

    @Throws(GoogleAccountNotSignedInThrowable::class, Throwable::class)
    suspend fun downloadBackupFile(fileId: String): InputStream = withContext(Dispatchers.IO) {
        log { "Downloading File - $fileId" }
        getDriveService()
            .Files()
            .get(fileId)
            .executeMediaAsInputStream()
    }

    @Throws(GoogleAccountNotSignedInThrowable::class)
    private fun getDriveService(): Drive {
        val account = signInService.getSignedInAccount()?.account
            ?: throw GoogleAccountNotSignedInThrowable()

        val credential = GoogleAccountCredential
            .usingOAuth2(context, listOf(Scopes.DRIVE_APPFOLDER)).apply {
                selectedAccount = account
            }

        return Drive.Builder(
            AndroidHttp.newCompatibleTransport(),
            JacksonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName(context.getString(R.string.app_name))
            .build()
    }

    private val backupParents: List<String>
        get() = listOf(APP_FOLDER_SPACE)
}

private const val BACKUP_MIME_TYPE = "application/octet-stream"
private const val APP_FOLDER_SPACE = "appDataFolder"

class GoogleAccountNotSignedInThrowable : Throwable("Google account not signed in")
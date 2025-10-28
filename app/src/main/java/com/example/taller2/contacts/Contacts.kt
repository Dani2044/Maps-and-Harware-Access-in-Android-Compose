package com.example.taller2.contacts

import android.Manifest
import com.example.taller2.R
import android.content.ContentResolver
import android.provider.ContactsContract
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ContactsScreen() {
    val context = LocalContext.current
    val contentResolver = context.contentResolver
    val contactsPermissionState = rememberPermissionState(Manifest.permission.READ_CONTACTS)

    Column (
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        when {
            contactsPermissionState.status.isGranted -> {
                DrawContacts(loadContacts(contentResolver))
            }

            contactsPermissionState.status.shouldShowRationale -> {
                Text(
                    "Please grant permission to display phone contacts properly",
                    modifier = Modifier.padding(30.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp
                )
                Button(
                    onClick = {contactsPermissionState.launchPermissionRequest()},
                    modifier = Modifier.fillMaxWidth().padding(30.dp)
                ) {
                    Text("Grant Permission")
                }
            }

            else -> {
                Text(
                    "Please grant permission to access this feature",
                    modifier = Modifier.padding(30.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp
                )
                Button(
                    onClick = {contactsPermissionState.launchPermissionRequest()},
                    modifier = Modifier.fillMaxWidth().padding(30.dp)
                ) {
                    Text("Grant Permission")
                }
            }
        }
    }
}
@Composable
fun DrawContacts(contacts: List<Contact>) {
    LazyColumn (
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) { items(contacts) { contact ->
        ElevatedCard {
            Row (
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(15.dp)
            ) {
                Image(
                    painterResource(R.drawable.contact_icon),
                    "Contacts",
                    modifier = Modifier.height(30.dp)
                )
                Spacer(modifier = Modifier.width(15.dp))
                Text(contact.id)
                Spacer(modifier = Modifier.width(15.dp))
                Text(contact.name)
            }
        }
    }}
}

fun loadContacts(contentResolver: ContentResolver): List<Contact> {
    val contacts = mutableListOf<Contact>()
    val projection = arrayOf(
        ContactsContract.CommonDataKinds.Phone._ID,
        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
    )

    val cursor = contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        projection,
        null,
        null,
        ContactsContract.CommonDataKinds.Phone._ID
    )

    cursor?.let {
        val idColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID)
        val nameColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)

        while(cursor.moveToNext()) {
            val id = cursor.getString(idColumn)
            val name = cursor.getString(nameColumn)
            contacts.add(Contact(id, name))
        }
    }
    cursor?.close()
    return contacts
}
package de.rogallab.mobile.ui.features.location.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

@Composable
fun SwitchWithLabel(
   label: String,
   checked: Boolean,
   onCheckedChange: (Boolean) -> Unit
) {

   val interactionSource = remember { MutableInteractionSource() }

   Row(
      modifier = Modifier
         .clickable(
            interactionSource = interactionSource,
            // This is for removing ripple when Row is clicked
            indication = null,
            role = Role.Switch,
            onClick = {
               onCheckedChange(!checked)
            }
         )
         .padding(8.dp),
      verticalAlignment = Alignment.CenterVertically

   ) {

      Text(
         modifier = Modifier.weight(0.9f),
         text = label)
      Switch(
         modifier = Modifier.weight(0.1f),
         checked = checked,
         onCheckedChange = {
            onCheckedChange(it)
         }
      )
   }
}
package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ForumComment
import com.example.data.local.entity.ForumPost
import com.example.ui.theme.EmergencyRed

@Composable
fun CommunityForumScreen(
    posts: List<ForumPost>,
    selectedPostId: Long?,
    postComments: List<ForumComment>,
    onSelectPost: (Long?) -> Unit,
    onLikePost: (ForumPost) -> Unit,
    onAddComment: (Long, String, String) -> Unit,
    onNewPostClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tags = listOf("Všetky", "Liečba & Lieky", "Výživa", "Očkovanie", "Správanie", "Všeobecné")
    var selectedTag by remember { mutableStateOf("Všetky") }

    val filteredPosts = if (selectedTag == "Všetky") {
        posts
    } else {
        posts.filter { it.petTypeTag.contains(selectedTag, ignoreCase = true) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Forum,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = "Fórum & Poradňa",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Tipy, otázky a skúsenosti",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Button(
                onClick = onNewPostClick,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                modifier = Modifier
                    .height(38.dp)
                    .testTag("new_post_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Pridať",
                    fontSize = 12.sp,
                    maxLines = 1,
                    softWrap = false,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Tags Filter
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            tags.forEach { tag ->
                FilterChip(
                    selected = selectedTag == tag,
                    onClick = { selectedTag = tag },
                    label = { Text(tag) }
                )
            }
        }

        if (filteredPosts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Žiadne diskusie v tejto kategórii.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredPosts, key = { it.id }) { post ->
                    val isExpanded = selectedPostId == post.id
                    ForumPostCard(
                        post = post,
                        isExpanded = isExpanded,
                        comments = if (isExpanded) postComments else emptyList(),
                        onToggleExpand = {
                            if (isExpanded) onSelectPost(null) else onSelectPost(post.id)
                        },
                        onLikeClick = { onLikePost(post) },
                        onAddComment = { commentText, authorName ->
                            onAddComment(post.id, commentText, authorName)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ForumPostCard(
    post: ForumPost,
    isExpanded: Boolean,
    comments: List<ForumComment>,
    onToggleExpand: () -> Unit,
    onLikeClick: () -> Unit,
    onAddComment: (comment: String, author: String) -> Unit
) {
    var newCommentText by remember { mutableStateOf("") }
    var authorName by remember { mutableStateOf("") }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = post.petTypeTag,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = post.dateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = post.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Autor: ${post.authorName}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action bar (Like + Comments toggle)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onLikeClick() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = if (post.isLikedByUser) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Lajk",
                        tint = if (post.isLikedByUser) EmergencyRed else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${post.likesCount}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (post.isLikedByUser) EmergencyRed else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onToggleExpand() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Komentáre",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isExpanded) "Skryť odpovede (${comments.size})" else "Odpovede (${post.commentsCount})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Expanded comments section
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Komentáre a odpovede:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            if (comments.isEmpty()) {
                                Text(
                                    text = "Zatiaľ žiadne komentáre. Napíšte prvú odpoveď!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                comments.forEach { comment ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = comment.authorName,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = comment.dateStr,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            text = comment.content,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Add comment box
                            OutlinedTextField(
                                value = authorName,
                                onValueChange = { authorName = it },
                                placeholder = { Text("Vaše meno") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = newCommentText,
                                    onValueChange = { newCommentText = it },
                                    placeholder = { Text("Pridať odpoveď...") },
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = {
                                        if (newCommentText.isNotBlank()) {
                                            onAddComment(newCommentText.trim(), authorName.trim())
                                            newCommentText = ""
                                        }
                                    },
                                    enabled = newCommentText.isNotBlank(),
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = "Odoslať",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

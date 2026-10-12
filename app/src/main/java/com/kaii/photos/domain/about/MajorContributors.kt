package com.kaii.photos.domain.about

import com.kaii.photos.R

val majorContributors: List<ContributorItem>
    get() = listOf(
        ContributorItem.Separator(
            title = R.string.contributors_separator_developer
        ),
        ContributorItem.Contributor(
            name = "Smelokuhle (MAHUNGELA)",
            description = R.string.contributor_kaii_desc,
            title = ContributorItem.Contributor.Title.Maintainer,
            avatarUrl = R.drawable.luma_developer_avatar,
            contributions = null,
            socials = listOf(
                SocialButton(
                    link = "https://github.com/nkosanamahungela-dev",
                    icon = SocialButton.Icon.Github
                )
            )
        )
    )

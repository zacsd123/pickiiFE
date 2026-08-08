package com.example.pickii.ui.feedback.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pickii.ui.theme.PickiiTextGray

private val FeedbackTipBackgroundColor = Color(0xFFFFFEEB)
private val FeedbackTipTextColor = Color(0xFF1E1E1C)

@Composable
fun FeedbackTipCard(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(17.dp))
                .background(FeedbackTipBackgroundColor)
                .padding(18.dp)
    ) {
        Text(
            text = "TIP!",
            color = FeedbackTipTextColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(13.dp))

        Text(
            text = "상호 평가는 프로젝트 종료 후 일정 기간 동안만 작성할 수 있습니다.",
            color = PickiiTextGray,
            fontSize = 12.sp,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(9.dp))

        Text(
            text = "평가 기간 내 최소 평가 인원을 완료하면, 평가 기간 종료 후 본인이 받은 AI 요약 피드백을 확인할 수 있습니다.",
            color = PickiiTextGray,
            fontSize = 12.sp,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(9.dp))

        Text(
            text = "후기는 익명으로 처리되며, 여러 팀원의 의견과 함께 종합되어 전달됩니다.",
            color = PickiiTextGray,
            fontSize = 12.sp,
            lineHeight = 18.sp
        )
    }
}

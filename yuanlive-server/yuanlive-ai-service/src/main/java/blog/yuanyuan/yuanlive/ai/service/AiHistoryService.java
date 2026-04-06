package blog.yuanyuan.yuanlive.ai.service;

import blog.yuanyuan.yuanlive.ai.domain.dto.SessionDeleteDTO;
import blog.yuanyuan.yuanlive.ai.domain.dto.SessionPageQueryDTO;
import blog.yuanyuan.yuanlive.ai.domain.dto.TitleUpdateDTO;
import blog.yuanyuan.yuanlive.ai.domain.vo.AiSessionVO;
import blog.yuanyuan.yuanlive.ai.domain.vo.ChatMessageVO;
import blog.yuanyuan.yuanlive.ai.domain.vo.PinSessionVO;
import blog.yuanyuan.yuanlive.common.result.Result;
import blog.yuanyuan.yuanlive.common.result.ResultPage;

import java.util.List;

public interface AiHistoryService {
    ResultPage<AiSessionVO> getUserSessionList(SessionPageQueryDTO queryDTO);

    Result<String> updateTitle(TitleUpdateDTO titleUpdateDTO);

    Result<PinSessionVO> pinSession(String conversationId);

    Result<PinSessionVO> unpinSession(String conversationId);

    Result<String> deleteSessions(SessionDeleteDTO deleteDTO);

    Result<String> deleteAllSessions();

    List<ChatMessageVO> getChatHistory(String conversationId);
}

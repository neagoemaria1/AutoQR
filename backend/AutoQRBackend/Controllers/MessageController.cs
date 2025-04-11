using AutoQRBackend.Services;
using AutoQRBackend.DTOs;
using Microsoft.AspNetCore.Mvc;
using AutoQRBackend.Models;

namespace AutoQRBackend.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class MessageController : ControllerBase
    {
        private readonly FcmService _fcmService;
        private readonly UserService _userService;

        public MessageController(FcmService fcmService, UserService userService)
        {
            _fcmService = fcmService;
            _userService = userService;
        }

        [HttpPost("send")]
        public async Task<IActionResult> SendMessage([FromBody] SendMessageRequest request)
        {
            
            var user = await _userService.GetUserByQrCodeAsync(request.ToQrCode);
            if (user == null)
            {
                return NotFound(new { message = "User not found." });
            }
            var success = await _fcmService.SendMessageAsync(user.DeviceToken, request.FromUsername, user.Username, request.Body);

            if (success)
            {
                return Ok(new { message = "Message sent successfully!" });
            }

            return BadRequest(new { message = "Failed to send message." });
        }
    }
}

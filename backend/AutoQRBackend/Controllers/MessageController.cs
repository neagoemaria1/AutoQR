using AutoQRBackend.Services;
using AutoQRBackend.DTOs;
using Microsoft.AspNetCore.Mvc;
using FirebaseAdmin.Auth;
using AutoQRBackend.Models;

namespace AutoQRBackend.Controllers
{
	[ApiController]
	[Route("api/[controller]")]
	public class MessageController : ControllerBase
	{
		private readonly FcmService _fcmService;
		private readonly UserService _userService;
		private readonly AuthService _authService;

		public MessageController(FcmService fcmService, UserService userService, AuthService authService)
		{
			_fcmService = fcmService;
			_userService = userService;
			_authService = authService;
		}


		[HttpPost("send")]
		public async Task<IActionResult> SendMessage([FromBody] SendMessageRequest request)
		{
			var user = await _userService.GetUserByQrCodeAsync(request.ToQrCode);
			if (user == null || string.IsNullOrEmpty(user.DeviceToken) || string.IsNullOrEmpty(user.Uid))
			{
				return NotFound(new { message = "Recipient not found or deviceToken/UID missing." });
			}

			var success = await _fcmService.SendMessageAsync(
				user.DeviceToken,
				request.FromUsername,
				user.Username,
				request.Body,
				user.Uid,
				request.MessageType ?? "alert",
				request.ReplyTo
			);

			if (success)
				return Ok(new { message = "Message sent successfully!" });

			return BadRequest(new { message = "Failed to send message." });
		}



		[HttpGet("inbox")]
		public async Task<IActionResult> GetInbox([FromHeader(Name = "Authorization")] string authorization)
		{
			var token = authorization.Replace("Bearer ", "").Trim();
			var user = await _authService.GetUserWithUidFromTokenAsync(token);

			if (user == null || string.IsNullOrEmpty(user.Uid))
				return Unauthorized(new { message = "Invalid or expired token." });

			var inbox = await _userService.GetInboxMessagesAsync(user.Uid);
			return Ok(inbox);
		}

		[HttpGet("predefined-messages")]
		public IActionResult GetPredefinedMessages()
		{
			var result = new PredefinedMessagesResponse
			{
				Alerts = new List<string>
			  {
					"You are blocking a car in the parking lot.",

					"You left your headlights on.",

					"Your rear tire is flat.",

					"Your car is about to be towed!",

					"You parked on a disabled spot.",

					"You left your window open.",

					"Your alarm is going off.",
			  },
				
					Replies = new List<string>
			  {
					"Thanks, I’m on my way!",

					"Sorry, I’ll move the car now.",

					"Appreciate the heads-up!",

					"Already fixed it, thanks!",

					"I’ll be there in 2 minutes."
			  }
			};

			return Ok(result);
		}



        [HttpPost("mark-as-read")]
        public async Task<IActionResult> MarkMessageAsRead([FromHeader(Name = "Authorization")] string authorization,[FromBody] string messageBody)
        {
            var token = authorization.Replace("Bearer ", "").Trim();
            var user = await _authService.GetUserWithUidFromTokenAsync(token);

            if (user == null || string.IsNullOrEmpty(user.Uid))
                return Unauthorized(new { message = "Invalid or expired token." });

            var result = await _userService.MarkMessageAsReadAsync(user.Uid, messageBody);
            return result ? Ok() : NotFound(new { message = "Message not found" });
        }


    }

}

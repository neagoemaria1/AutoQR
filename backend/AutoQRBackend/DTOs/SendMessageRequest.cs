namespace AutoQRBackend.DTOs
{
    public class SendMessageRequest
    {
        public required string FromUsername { get; set; }  
        public required string ToQrCode { get; set; }     
        public required string Body { get; set; }          
    }
}
